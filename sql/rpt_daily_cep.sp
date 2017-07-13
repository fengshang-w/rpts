/*
Navicat MySQL Data Transfer

Source Server         : 163
Source Server Version : 50508
Source Host           : wde2d.ndscd.com:3306
Source Database       : cep

Target Server Type    : MYSQL
Target Server Version : 50508
File Encoding         : 65001

Date: 2014-10-22 18:25:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Procedure structure for rpt_daily_cep
-- ----------------------------
DROP PROCEDURE IF EXISTS `rpt_daily_cep`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `rpt_daily_cep`()
BEGIN
	declare v_rpt_date date;
	declare v_date date;
	declare v_idx int default 1;
	declare v_exch_rate int default 19.5;
	declare v_add_fee_rate int default 0.2;

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_cep;
	CREATE TEMPORARY TABLE temp_rpt_daily_cep(
		id int(2) NOT NULL AUTO_INCREMENT,
		item_code VARCHAR(32) NOT NULL,
		item_desc1 VARCHAR(32) NOT NULL,
		item_desc2 VARCHAR(32) NULL,
		d1 float NULL,
		d2 float NULL,
		d3 float NULL,
		d4 float NULL,
		d5 float NULL,
		d6 float NULL,
		d7 float NULL,
		dm float NULL,
		PRIMARY key(id)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_cep2;
	CREATE TEMPORARY TABLE temp_rpt_daily_cep2(
		rpt_date varchar(10) NOT NULL,
		upload_j_count int(11) default 0 not null,
		deliver_j_count int(11) default 0 not null,
		hc_jp int(11) default 0 not null,
		hc_cd int(11) default 0 not null,
		deliver_amt int(11) default 0 not null,
		unit_deliver_price float default 0 not null,
		unit_cost_scan float default 0 not null,
		unit_cost_entry float default 0 not null,
		unit_cost_review float default 0 not null,
		unit_cost_total float default 0 not null,
		cost_scan int(11) default 0 not null,
		cost_entry int(11) default 0 not null,
		cost_review int(11) default 0 not null,
		cost_total int(11) default 0 not null,
		gross_margin int(11) default 0 not null,
		gross_margin_rate float default 0 not null,
		PRIMARY key(rpt_date)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_batch3;
	CREATE TEMPORARY TABLE temp_rpt_batch3(
		batch_id int NOT NULL,
		start_date varchar(10),
		end_date varchar(10),
		business_type int NOT NULL,
		j_count int(11) NULL,
		PRIMARY key(batch_id)
	);

	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('upload_j_count','受注（件）',null);
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('deliver_j_count','納品（件）',null);
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('hc_jp','入力人数','日本');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('hc_cd','','成都');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('deliver_amt','納品金額（円）',null);
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('unit_deliver_price','納品単価（円/件）',null);
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('unit_cost_scan','単位原価（円/件）','スキャン');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('unit_cost_entry','','入力');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('unit_cost_review','','目検');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('unit_cost_total','','小計');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('cost_scan','合計原価（円）','スキャン');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('cost_entry','','入力');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('cost_review','','目検');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('cost_total','','合計');
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('gross_margin','粗利（円）',null);
	insert into temp_rpt_daily_cep(item_code,item_desc1,item_desc2) values('gross_margin_rate','粗利率',null);

	set v_date = DATE_ADD(curdate(),INTERVAL -7 day);
	set v_idx = 1;
	while v_idx <= 7 do
		insert into temp_rpt_daily_cep2(rpt_date) values(substring(v_date,1,10));
		set v_date = DATE_ADD(v_date,INTERVAL 1 day);
		set v_idx= v_idx+1;
	end while;

	set v_rpt_date = substring(DATE_ADD(curdate(),INTERVAL -1 day),1,10);
	set v_date = DATE_ADD(curdate(),INTERVAL -7 day);

	insert into temp_rpt_daily_cep2(rpt_date,upload_j_count)
		select rpt_date,upload_j_count
		from
		(select substring(a.start_datetime,1,10) rpt_date,sum(a.j_count) upload_j_count
		from rpt_batch3 a
		where substring(a.start_datetime,1,10)>=v_date and substring(a.start_datetime,1,10)<=v_rpt_date
		group by rpt_date)
		AS a ON DUPLICATE KEY UPDATE upload_j_count=a.upload_j_count;

	insert into temp_rpt_batch3(batch_id,start_date,end_date,business_type,j_count)
		select a.batch_id,substring(a.start_datetime,1,10),substring(a.end_datetime,1,10),a.business_type,a.j_count
		from rpt_batch3 a
		,(select batch_id from batch_list where assemble='F'
			union select batch_id from cep_h.h_batch_list where assemble='F' and substring(last_update_datetime,1,10)>=v_date) b
		where a.batch_id=b.batch_id and a.prod_type=1
		and substring(a.end_datetime,1,10)>=v_date and substring(a.end_datetime,1,10)<=v_rpt_date;

	insert into temp_rpt_daily_cep2(rpt_date,deliver_j_count,deliver_amt,unit_deliver_price)
		select rpt_date,deliver_j_count,deliver_amt,unit_deliver_price
		from
		(select a.end_date rpt_date,ifnull(sum(a.j_count),0) deliver_j_count,ifnull(sum(a.j_count*b.price),0) deliver_amt,ifnull(sum(a.j_count*b.price)/sum(a.j_count),0) unit_deliver_price
		from temp_rpt_batch3 a
		left join rpt_price b on a.business_type=b.business_type and a.end_date>=b.start_date and a.end_date<=b.end_date
		group by rpt_date)
		AS a ON DUPLICATE KEY UPDATE deliver_j_count=a.deliver_j_count,deliver_amt=a.deliver_amt,unit_deliver_price=a.unit_deliver_price;

	insert into temp_rpt_daily_cep2(rpt_date,hc_jp)
		select rpt_date,hc_jp
		from
		(select a.rpt_date, count(distinct b.user_id) hc_jp
		from rpt_perf3 a,staff b
		where a.user_id=b.user_id and b.loc_id=1 and a.rpt_date>=v_date and a.rpt_date<=v_rpt_date and b.user_id>0
		group by rpt_date)
		AS a ON DUPLICATE KEY UPDATE hc_jp=a.hc_jp;

	insert into temp_rpt_daily_cep2(rpt_date,hc_cd)
		select rpt_date,hc_cd
		from
		(select a.rpt_date, count(distinct b.user_id) hc_cd
		from rpt_perf3 a,staff b
		where a.user_id=b.user_id and b.loc_id<>1 and a.rpt_date>=v_date and a.rpt_date<=v_rpt_date and b.user_id>0
		group by rpt_date)
		AS a ON DUPLICATE KEY UPDATE hc_cd=a.hc_cd;

	insert into temp_rpt_daily_cep2(rpt_date,cost_scan,cost_review)
		select rpt_date,cost_scan,cost_review
		from
		(select a.end_date rpt_date,ifnull(sum(a.j_count*b.scan_cost),0) cost_scan,ifnull(sum(a.j_count*b.review_cost),0) cost_review
		from temp_rpt_batch3 a,rpt_unit_code b
		where a.business_type=b.business_type and a.end_date>=b.start_date and a.end_date<=b.end_date
		group by a.end_date)
		AS a ON DUPLICATE KEY UPDATE cost_scan=a.cost_scan,cost_review=a.cost_review;

	insert into temp_rpt_daily_cep2(rpt_date,cost_entry)
		select rpt_date,cost_entry
		from
		(select a.end_date rpt_date,(sum((b.t_length_20-b.wc_length_20*c.error_weight_20)*c.price_20)/10000
																+sum((b.t_length_21-b.wc_length_21*c.error_weight_21)*c.price_21)/10000
																+sum((b.t_length_22-b.wc_length_22*c.error_weight_22)*c.price_22)/10000
																+sum((b.t_length_23-b.wc_length_23*c.error_weight_23)*c.price_23)/10000
																+sum((b.p_count-b.wp_count+b.r_count-b.wr_count)*ifnull(d.pre_price,0.02))
																)*v_exch_rate*(1+v_add_fee_rate) cost_entry
		from temp_rpt_batch3 a
		join rpt_perf3 b on a.batch_id=b.batch_id
		left join rpt_perf_unit_detail c on b.business_type=c.business_type and b.rpt_date=c.rpt_date
		LEFT JOIN rpt_perf_unit_price d ON b.business_type=d.business_type and b.rpt_date>=d.start_date and b.rpt_date<=d.end_date
		where b.user_id>0
		group by a.end_date)
		AS a ON DUPLICATE KEY UPDATE cost_entry=a.cost_entry;

	update temp_rpt_daily_cep2 set unit_cost_scan=ifnull(cost_scan/deliver_j_count,0), unit_cost_review=ifnull(cost_review/deliver_j_count,0), unit_cost_entry=ifnull(cost_entry/deliver_j_count,0);
	update temp_rpt_daily_cep2 set unit_cost_total=unit_cost_scan+unit_cost_review+unit_cost_entry, cost_total=cost_scan+cost_review+cost_entry;
	update temp_rpt_daily_cep2 set gross_margin=ifnull(deliver_amt-cost_total,0), gross_margin_rate=ifnull((deliver_amt-cost_total)/deliver_amt,0);


	set v_idx = 1;
	while v_idx <= 7 do
		set @v_date = v_date;

		set @v_item_code = 'upload_j_count';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'deliver_j_count';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'hc_jp';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'hc_cd';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'deliver_amt';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'unit_deliver_price';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'unit_cost_scan';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'unit_cost_entry';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'unit_cost_review';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'unit_cost_total';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'cost_scan';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'cost_entry';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'cost_review';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'cost_total';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'gross_margin';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;

		set @v_item_code = 'gross_margin_rate';
		set @v_sql = concat('update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.d',v_idx,'=b.',@v_item_code,' where a.item_code=? and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_item_code,@v_date;


		set v_date = DATE_ADD(v_date,INTERVAL 1 day);
		set v_idx= v_idx+1;
	end while;

	set v_date=DATE_ADD(v_rpt_date,interval -day(v_rpt_date)+1 day);

	truncate table temp_rpt_batch3;
	truncate table temp_rpt_daily_cep2;

	insert into temp_rpt_daily_cep2(rpt_date) values('TTL');

	insert into temp_rpt_daily_cep2(rpt_date,upload_j_count)
		select rpt_date,upload_j_count
		from
		(select 'TTL' rpt_date,ifnull(sum(a.j_count),0) upload_j_count
		from rpt_batch3 a
		where substring(a.start_datetime,1,10)>=v_date and substring(a.start_datetime,1,10)<=v_rpt_date)
		AS a ON DUPLICATE KEY UPDATE upload_j_count=a.upload_j_count;

	insert into temp_rpt_batch3(batch_id,start_date,end_date,business_type,j_count)
		select a.batch_id,substring(a.start_datetime,1,10),substring(a.end_datetime,1,10),a.business_type,a.j_count
		from rpt_batch3 a
		,(select batch_id from batch_list where assemble='F'
			union select batch_id from cep_h.h_batch_list where assemble='F' and substring(last_update_datetime,1,10)>=v_date) b
		where a.batch_id=b.batch_id and a.prod_type=1
		and substring(a.end_datetime,1,10)>=v_date and substring(a.end_datetime,1,10)<=v_rpt_date;

	insert into temp_rpt_daily_cep2(rpt_date,deliver_j_count,deliver_amt,unit_deliver_price)
		select rpt_date,deliver_j_count,deliver_amt,unit_deliver_price
		from
		(select 'TTL' rpt_date,ifnull(sum(a.j_count),0) deliver_j_count,ifnull(sum(a.j_count*b.price),0) deliver_amt,ifnull(sum(a.j_count*b.price)/sum(a.j_count),0) unit_deliver_price
		from temp_rpt_batch3 a
		left join rpt_price b on a.business_type=b.business_type and a.end_date>=b.start_date and a.end_date<=b.end_date)
		AS a ON DUPLICATE KEY UPDATE deliver_j_count=a.deliver_j_count,deliver_amt=a.deliver_amt,unit_deliver_price=a.unit_deliver_price;

	insert into temp_rpt_daily_cep2(rpt_date,hc_jp)
		select rpt_date,hc_jp
		from
		(select 'TTL' rpt_date, count(distinct b.user_id) hc_jp
		from rpt_perf3 a,staff b
		where a.user_id=b.user_id and b.loc_id=1 and a.rpt_date>=v_date and a.rpt_date<=v_rpt_date and b.user_id>0)
		AS a ON DUPLICATE KEY UPDATE hc_jp=a.hc_jp;

	insert into temp_rpt_daily_cep2(rpt_date,hc_cd)
		select rpt_date,hc_cd
		from
		(select 'TTL' rpt_date, count(distinct b.user_id) hc_cd
		from rpt_perf3 a,staff b
		where a.user_id=b.user_id and b.loc_id<>1 and a.rpt_date>=v_date and a.rpt_date<=v_rpt_date and b.user_id>0)
		AS a ON DUPLICATE KEY UPDATE hc_cd=a.hc_cd;

	insert into temp_rpt_daily_cep2(rpt_date,cost_scan,cost_review)
		select rpt_date,cost_scan,cost_review
		from
		(select 'TTL' rpt_date,ifnull(sum(a.j_count*b.scan_cost),0) cost_scan,ifnull(sum(a.j_count*b.review_cost),0) cost_review
		from temp_rpt_batch3 a,rpt_unit_code b
		where a.business_type=b.business_type and a.end_date>=b.start_date and a.end_date<=b.end_date)
		AS a ON DUPLICATE KEY UPDATE cost_scan=a.cost_scan,cost_review=a.cost_review;

	insert into temp_rpt_daily_cep2(rpt_date,cost_entry)
		select rpt_date,cost_entry
		from
		(select 'TTL' rpt_date,ifnull((sum((b.t_length_20-b.wc_length_20*c.error_weight_20)*c.price_20)/10000
																+sum((b.t_length_21-b.wc_length_21*c.error_weight_21)*c.price_21)/10000
																+sum((b.t_length_22-b.wc_length_22*c.error_weight_22)*c.price_22)/10000
																+sum((b.t_length_23-b.wc_length_23*c.error_weight_23)*c.price_23)/10000
																+sum((b.p_count-b.wp_count+b.r_count-b.wr_count)*ifnull(d.pre_price,0.02))
																)*v_exch_rate*(1+v_add_fee_rate),0) cost_entry
		from temp_rpt_batch3 a
		join rpt_perf3 b on a.batch_id=b.batch_id
		left join rpt_perf_unit_detail c on b.business_type=c.business_type and b.rpt_date=c.rpt_date
		left join rpt_perf_unit_price d on b.business_type=d.business_type and b.rpt_date>=d.start_date and b.rpt_date<=d.end_date
		where b.user_id>0)
		AS a ON DUPLICATE KEY UPDATE cost_entry=a.cost_entry;

	update temp_rpt_daily_cep2 set unit_cost_scan=cost_scan/deliver_j_count, unit_cost_review=cost_review/deliver_j_count, unit_cost_entry=cost_entry/deliver_j_count;
	update temp_rpt_daily_cep2 set unit_cost_total=unit_cost_scan+unit_cost_review+unit_cost_entry, cost_total=cost_scan+cost_review+cost_entry;
	update temp_rpt_daily_cep2 set gross_margin=deliver_amt-cost_total, gross_margin_rate=(deliver_amt-cost_total)/deliver_amt;

	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.upload_j_count where a.item_code='upload_j_count';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.deliver_j_count where a.item_code='deliver_j_count';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.hc_jp where a.item_code='hc_jp';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.hc_cd where a.item_code='hc_cd';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.deliver_amt where a.item_code='deliver_amt';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.unit_deliver_price where a.item_code='unit_deliver_price';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.unit_cost_scan where a.item_code='unit_cost_scan';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.unit_cost_entry where a.item_code='unit_cost_entry';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.unit_cost_review where a.item_code='unit_cost_review';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.unit_cost_total where a.item_code='unit_cost_total';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.cost_scan where a.item_code='cost_scan';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.cost_entry where a.item_code='cost_entry';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.cost_review where a.item_code='cost_review';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.cost_total where a.item_code='cost_total';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.gross_margin where a.item_code='gross_margin';
	update temp_rpt_daily_cep a,temp_rpt_daily_cep2 b set a.dm=b.gross_margin_rate where a.item_code='gross_margin_rate';

	select * from temp_rpt_daily_cep order by id;

END
;;
DELIMITER ;
