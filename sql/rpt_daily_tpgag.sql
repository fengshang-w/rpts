/*
Navicat MySQL Data Transfer

Source Server         : 161
Source Server Version : 50508
Source Host           : 10.88.1.161:3306
Source Database       : tpg_ag

Target Server Type    : MYSQL
Target Server Version : 50508
File Encoding         : 65001

Date: 2014-06-27 08:59:42
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Procedure structure for `rpt_daily_tpgag`
-- ----------------------------
DROP PROCEDURE IF EXISTS `rpt_daily_tpgag`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `rpt_daily_tpgag`()
BEGIN
	declare v_rpt_date date;
	declare v_date date;
	declare v_idx int default 1;
	declare v_null_dept_id varchar(16) default '99999';

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_tpgag;
	CREATE TEMPORARY TABLE temp_rpt_daily_tpgag(
		dept_id VARCHAR(16) NOT NULL,
		dept_desc VARCHAR(128) NOT NULL,
		d1 int(11) NULL,
		d2 int(11) NULL,
		d3 int(11) NULL,
		d4 int(11) NULL,
		d5 int(11) NULL,
		d6 int(11) NULL,
		d7rd int(11) NULL,
		d7rw int(11) NULL,
		d7rm int(11) NULL,
		d7rmp int(11) NULL,
		d7uq int(11) NULL,
		d7ttl int(11) NULL,
		dm int(11) NULL,
		PRIMARY key(dept_id)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_tpgag2;
	CREATE TEMPORARY TABLE temp_rpt_daily_tpgag2(
		rpt_date date NOT NULL,
		dept_id VARCHAR(16) NOT NULL,
		ag_count int(11) NULL,
		PRIMARY key(rpt_date,dept_id)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_tpgag3;
	CREATE TEMPORARY TABLE temp_rpt_daily_tpgag3(
		dept_id VARCHAR(16) NOT NULL,
		rd int(11) NULL,
		rw int(11) NULL,
		rm int(11) NULL,
		rmp int(11) NULL,
		uq int(11) NULL,
		ttl int(11) NULL,
		PRIMARY key(dept_id)
	);

	set v_rpt_date = substring(DATE_ADD(curdate(),INTERVAL -1 day),1,10);
	delete from rpt_ag_track where rpt_date=v_rpt_date;

	insert into rpt_ag_track(rpt_date,dept_id,ag_id,ts)
	select substring(a.ts,1,10) rpt_date,ifnull(b.dept_id,'') sdept_id,a.ag_id,max(a.ts) ts
	 from ag_track a
	 left join job b on a.job_id=b.id
	 where substring(a.ts,1,10)=v_rpt_date
	 group by rpt_date,sdept_id,a.ag_id;

	update rpt_ag_track aa,
		(select a.rpt_date,a.dept_id,a.ag_id,max(b.ts) last_ts
		from rpt_ag_track a,rpt_ag_track b
		where a.dept_id=b.dept_id and a.ag_id=b.ag_id and a.rpt_date>b.rpt_date
		group by a.rpt_date,a.dept_id,a.ag_id) bb
		set aa.last_ts=bb.last_ts
		where aa.dept_id=bb.dept_id and aa.ag_id=bb.ag_id and aa.rpt_date=bb.rpt_date
		and aa.rpt_date=v_rpt_date;

	update rpt_ag_track
	 set datediff=datediff(ts,last_ts)
	 where last_ts is not null and datediff is null and rpt_date=v_rpt_date;

 
	set v_date = DATE_ADD(curdate(),INTERVAL -7 day);

	insert into temp_rpt_daily_tpgag(dept_id,dept_desc)
		select distinct a.dept_id,c.正式名称
		from rpt_ag_track a,TM_DEPT c
		where a.dept_id=c.事務所番号 and a.rpt_date>=v_date
		order by a.dept_id;

	insert into temp_rpt_daily_tpgag(dept_id,dept_desc) values(v_null_dept_id,'その他(管理者テスト)');

	insert into temp_rpt_daily_tpgag2(rpt_date,dept_id,ag_count)
		select rpt_date,dept_id,count(distinct ag_id) ag_count
		from rpt_ag_track
		where rpt_date>=v_date and rpt_date<v_rpt_date
		group by rpt_date,dept_id;

	insert into temp_rpt_daily_tpgag2(rpt_date,dept_id,ag_count)
		select a.rpt_date,v_null_dept_id,count(distinct ag_id)
		from rpt_ag_track a
		where a.rpt_date>=v_date and rpt_date<v_rpt_date
		and not exists(select 0 from rpt_ag_track b
			where a.ag_id=b.ag_id and a.rpt_date=b.rpt_date and b.dept_id is not null and b.dept_id<>'')
		group by rpt_date;

	set v_idx = 1;
	while v_idx <= 6 do
		set @v_date = v_date;
		set @v_sql = concat('update temp_rpt_daily_tpgag a,temp_rpt_daily_tpgag2 b set a.d',v_idx,'=b.ag_count where a.dept_id=b.dept_id and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_date;
		set v_date = DATE_ADD(v_date,INTERVAL 1 day);
		set v_idx= v_idx+1;
	end while;


	insert into temp_rpt_daily_tpgag3(dept_id,rd,rw,rm,rmp,uq,ttl)
		select dept_id
		,sum(case when datediff<=1 then 1 else 0 end) rd 
		,sum(case when datediff>1 and datediff<=7 then 1 else 0 end) rw
		,sum(case when datediff>7 and datediff<=30 then 1 else 0 end) rm
		,sum(case when datediff>30 then 1 else 0 end) rmp
		,sum(case when datediff is null then 1 else 0 end) uq
		,sum(1) ttl 
		from rpt_ag_track
		where rpt_date=v_rpt_date
		group by dept_id;

	insert into temp_rpt_daily_tpgag3(dept_id,rd,rw,rm,rmp,uq,ttl)
		select v_null_dept_id,sum(case when datediff<=1 then 1 else 0 end) d1 
		,sum(case when datediff>1 and datediff<=7 then 1 else 0 end) dw
		,sum(case when datediff>7 and datediff<=30 then 1 else 0 end) dm
		,sum(case when datediff>30 then 1 else 0 end) dy
		,sum(case when datediff is null then 1 else 0 end) uq
		,sum(1) ttl
		from rpt_ag_track a
		where a.rpt_date=v_rpt_date
		and not exists(select 0 from rpt_ag_track b
			where a.ag_id=b.ag_id and a.rpt_date=b.rpt_date and b.dept_id is not null and b.dept_id<>'');

	update temp_rpt_daily_tpgag a,temp_rpt_daily_tpgag3 b
		set a.d7rd=b.rd, a.d7rw=b.rw, a.d7rm=b.rm, a.d7rmp=b.rmp, a.d7uq=b.uq, a.d7ttl=b.ttl
		where a.dept_id=b.dept_id;

	-- 月合计
	set v_date=DATE_ADD(v_rpt_date,interval -day(v_rpt_date)+1 day);
	delete from temp_rpt_daily_tpgag2;

	insert into temp_rpt_daily_tpgag2(rpt_date,dept_id,ag_count)
		select 'TTL',dept_id,count(distinct ag_id) ag_count
		from rpt_ag_track
		where rpt_date>=v_date and rpt_date<=v_rpt_date
		group by dept_id;

	insert into temp_rpt_daily_tpgag2(rpt_date,dept_id,ag_count)
		select 'TTL',v_null_dept_id,count(distinct ag_id)
		from rpt_ag_track a
		where a.rpt_date>=v_date and rpt_date<=v_rpt_date
		and not exists(select 0 from rpt_ag_track b
			where a.ag_id=b.ag_id and substring(a.rpt_date,1,7)=substring(b.rpt_date,1,7) and b.dept_id is not null and b.dept_id<>'');

	update temp_rpt_daily_tpgag a,temp_rpt_daily_tpgag2 b set a.dm=b.ag_count where a.dept_id=b.dept_id;

	drop TEMPORARY table if exists temp_rpt_daily_tpgag_t;
	create TEMPORARY table temp_rpt_daily_tpgag_t as select * from temp_rpt_daily_tpgag;

	insert into temp_rpt_daily_tpgag(dept_id,dept_desc,d1,d2,d3,d4,d5,d6,d7rd,d7rw,d7rm,d7rmp,d7uq,d7ttl,dm)
		select 'TTL', '総数',sum(d1),sum(d2),sum(d3),sum(d4),sum(d5),sum(d6),sum(d7rd),sum(d7rw),sum(d7rm),sum(d7rmp),sum(d7uq),sum(d7ttl),sum(dm)
		from temp_rpt_daily_tpgag_t a;

	select * from temp_rpt_daily_tpgag order by dept_id;

END
;;
DELIMITER ;
