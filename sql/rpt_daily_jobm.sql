/*
Navicat MySQL Data Transfer

Source Server         : 161
Source Server Version : 50508
Source Host           : 10.88.1.161:3306
Source Database       : tpg_ag

Target Server Type    : MYSQL
Target Server Version : 50508
File Encoding         : 65001

Date: 2014-06-27 08:59:32
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Procedure structure for `rpt_daily_jobm`
-- ----------------------------
DROP PROCEDURE IF EXISTS `rpt_daily_jobm`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `rpt_daily_jobm`()
BEGIN
	declare v_rpt_date date;
	declare v_date date;
	declare v_idx int default 1;

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_jobm;
	CREATE TEMPORARY TABLE temp_rpt_daily_jobm(
		id int(2) NOT NULL AUTO_INCREMENT,
		url VARCHAR(128) NOT NULL,
		url_desc VARCHAR(32) NOT NULL,
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
		PRIMARY key(id)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_jobm2;
	CREATE TEMPORARY TABLE temp_rpt_daily_jobm2(
		rpt_date date NOT NULL,
		url VARCHAR(128) NOT NULL,
		ip_count int(11) NULL,
		PRIMARY key(rpt_date,url)
	);

	DROP TEMPORARY TABLE IF EXISTS temp_rpt_daily_jobm3;
	CREATE TEMPORARY TABLE temp_rpt_daily_jobm3(
		url VARCHAR(128) NOT NULL,
		rd int(11) NULL,
		rw int(11) NULL,
		rm int(11) NULL,
		rmp int(11) NULL,
		uq int(11) NULL,
		ttl int(11) NULL,
		PRIMARY key(url)
	);

	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/jobm/','トップページ');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/js/job_disp.asp','PC版-検索');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/js/send_mail.asp','PC版-応募');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/regist/acount.asp','PC版-仮登録');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/jobm/jobList','スマホ版-検索');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/jobm/job/applyJob','スマホ版-応募');
	insert into temp_rpt_daily_jobm(url,url_desc) values('/job/jobm/join','スマホ版-仮登録');

	set v_rpt_date = substring(DATE_ADD(curdate(),INTERVAL -1 day),1,10);

	insert into pv_mypage select * from pv where url like '/mypage%' and left(ts,10)<=v_rpt_date;
	delete from pv where url like '/mypage%' and left(ts,10)<=v_rpt_date;

	delete from jobm_url_report where rpt_date=v_rpt_date;

	insert into jobm_url_report(rpt_date,ip,url,access_datetime)
		select substring(ts,1,10) rpt_date,ip,url,max(ts) access_datetime
		from pv
		where substring(ts,1,10)=v_rpt_date
		group by rpt_date,ip,url;

	update jobm_url_report aa,
		(select a.rpt_date,a.ip,a.url,max(b.ts) last_access_datetime
		from jobm_url_report a,pv b
		where a.ip=b.ip and a.url=b.url and a.rpt_date>substring(b.ts,1,10)
		group by a.rpt_date,a.ip,access_datetime) bb
		set aa.last_access_datetime=bb.last_access_datetime
		where aa.ip=bb.ip and aa.url=bb.url and aa.rpt_date=bb.rpt_date
		and aa.last_access_datetime is null and aa.rpt_date=v_rpt_date;

	update jobm_url_report
	 set datediff=datediff(access_datetime,last_access_datetime)
	 where last_access_datetime is not null and datediff is null and rpt_date=v_rpt_date;

 
	set v_date = DATE_ADD(curdate(),INTERVAL -7 day);

	insert into temp_rpt_daily_jobm2(rpt_date,url,ip_count)
		select rpt_date,url,count(ip)
		from jobm_url_report
		where rpt_date>=v_date and rpt_date<v_rpt_date
		group by rpt_date,url;

	set v_idx = 1;
	while v_idx <= 6 do
		set @v_date = v_date;
		set @v_sql = concat('update temp_rpt_daily_jobm a,temp_rpt_daily_jobm2 b set a.d',v_idx,'=b.ip_count where a.url=b.url and b.rpt_date=?');
		prepare stmt from @v_sql;
		execute stmt using @v_date;
		set v_date = DATE_ADD(v_date,INTERVAL 1 day);
		set v_idx= v_idx+1;
	end while;


	insert into temp_rpt_daily_jobm3(url,rd,rw,rm,rmp,uq,ttl)
		select url
		,sum(case when datediff<=1 then 1 else 0 end) rd 
		,sum(case when datediff>1 and datediff<=7 then 1 else 0 end) rw
		,sum(case when datediff>7 and datediff<=30 then 1 else 0 end) rm
		,sum(case when datediff>30 then 1 else 0 end) rmp
		,sum(case when datediff is null then 1 else 0 end) uq
		,sum(1) ttl 
		from jobm_url_report
		where rpt_date=v_rpt_date
		group by url;

	update temp_rpt_daily_jobm a,temp_rpt_daily_jobm3 b
		set a.d7rd=b.rd, a.d7rw=b.rw, a.d7rm=b.rm, a.d7rmp=b.rmp, a.d7uq=b.uq, a.d7ttl=b.ttl
		where a.url=b.url;

	set v_date=DATE_ADD(v_rpt_date,interval -day(v_rpt_date)+1 day);
	delete from temp_rpt_daily_jobm2;

	insert into temp_rpt_daily_jobm2(rpt_date,url,ip_count)
		select 'TTL',url,count(ip)
		from jobm_url_report
		where rpt_date>=v_date and rpt_date<=v_rpt_date
		group by url;

	update temp_rpt_daily_jobm a,temp_rpt_daily_jobm2 b set a.dm=b.ip_count where a.url=b.url;

	select * from temp_rpt_daily_jobm order by id;

END
;;
DELIMITER ;
