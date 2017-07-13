package com.rpt.cep;

import com.rpt.util.SendMailUtil;
import com.synnex.cdc.waf.model.Table;
import com.synnex.cdc.waf.sql.SQLDAO;
import com.synnex.cdc.waf.tmpl.QBF.model.QBFResponse;
import com.synnex.cdc.waf.util.Log;
import com.synnex.cdc.waf.util.ServiceLocator;
import com.synnex.cdc.waf.web.ctrl.ConfigDoc;

public class CEPOnlineUser {

	private static SQLDAO dbcep;

	public static void main(String[] args){
		try{
			String sendFrom = "cepreport@ndscd.com";
			String sendTo = "walkerh@ndscd.com";
			String sendCC = null;
			if(args.length>0){
				sendTo=args[0];
			}
			if(args.length>1){
				sendCC=args[1];
			}
			init();
			sendRptProductivity(searchRptProductivity(), sendFrom, sendTo, sendCC);
		}catch(Exception e){
			Log.logErr(e);
		}
	}

	private static void init() throws Exception {
		ConfigDoc.init("config/web-config_de.xml");
		Log.init();
		dbcep = (SQLDAO) ServiceLocator.getBean("daocep");
	}

	private static QBFResponse searchRptProductivity()
			throws Exception {
		
		String sql = "select date_add(now(), interval 1 hour)";
		String nowTime = dbcep.search(sql).getCellValue(0, 0, true);

		sql = "select a.job_type,c.group_name,a.user_id,a.login_id,b.first_name,TIMEDIFF(now(),last_login_datetime) last_login_datetime,e.business_code"
				+ " from staff_login a"
				+ " inner join staff b on a.user_id=b.user_id"
				+ " inner join staff_group c on b.group_id=c.group_id"
				+ " left join (select user_id,min(business_type) business_type from task_queue where status='Y' group by user_id) d on a.user_id=d.user_id"
				+ " left join business_code e on d.business_type=e.business_type"
				+ " where date_add(a.active_time, interval 15 minute) > now()"
				+ " order by a.job_type,c.group_name,a.login_id";

		QBFResponse qrsp = new QBFResponse(dbcep.search(sql));
		qrsp.setElement("nowTime", nowTime);
		return qrsp;
	}

	private static void sendRptProductivity(QBFResponse qrsp, String sendFrom, String sendTo, String sendCC) throws Exception {
		String nowTime = qrsp.getElement("nowTime").toString();
		Table table = qrsp.getTable();

		String subject = "CEPオンラインユーザーのお知らせ["+nowTime+"]";

		StringBuffer content = new StringBuffer();
		content.append("<style>");
		content.append("body {FONT-FAMILY: Arial; FONT-SIZE: 9pt;}");
		content.append(".tableBody {BACKGROUND:#18618A;border: 1 solid #18618A;}");
		content
		.append(".rowHead  {padding-left: 2; font-family: Arial;font-weight:bold; font-size: 8pt; color: #000000; BACKGROUND: #BDCFCF; HEIGHT: 20px;text-align: center;}");
		content
				.append(".rowView0  {padding-left: 5; padding-right: 5; font-family: Arial;  font-size: 8pt; color: #000000; BACKGROUND: #FFFFFF; HEIGHT: 18px;}");
		content
				.append(".rowView1  {padding-left: 5; padding-right: 5; font-family: Arial;  font-size: 8pt; color: #000000; BACKGROUND: #EBF3F3; HEIGHT: 18px;}");
		content.append("</style>");

		content.append("皆さん");
		content.append("<br><br>現時点["+nowTime+"]のCEPオンラインユーザーは、"+table.getRowCount()+"名です");
		content.append("<br><br>");
		
		if(table.getRowCount()>0){
			content.append("<table border=0 cellspacing=1 cellpadding=0 class='tableBody'>");
			content.append("<tr>");
			content.append("<td class='rowHead' nowrap>SN</td>");
			content.append("<td class='rowHead' nowrap>JOB TYPE</td>");
			content.append("<td class='rowHead' nowrap>GROUP</td>");
			content.append("<td class='rowHead' nowrap>USER ID</td>");
			content.append("<td class='rowHead' nowrap>LOGIN ID</td>");
			content.append("<td class='rowHead' nowrap>NAME</td>");
			content.append("<td class='rowHead' nowrap>LOGIN TIME</td>");
			content.append("<td class='rowHead' nowrap>BUSINESS TYPE</td>");
			content.append("</tr>");

			String classname="class=rowView";
			for (int i = 0; i < table.getRowCount(); i++) {
				classname =" class=rowView"+i%2;
				content.append("<tr>");
				content.append("<td "+classname+" nowrap>"+(i+1)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "job_type",true)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "group_name",true)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "user_id",true)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "login_id",true)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "first_name",true)+"</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "last_login_datetime",true)+"前</td>");
				content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "business_code",true)+"</td>");
				content.append("</tr>");
			}
			content.append("</table>");
		}

		content.append("<br><br>--CEP MAIL--<br>");

		SendMailUtil.sendMail(sendFrom, sendTo, sendCC, null, subject, content.toString());
	}

}
