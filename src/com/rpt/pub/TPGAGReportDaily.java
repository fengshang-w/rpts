package com.rpt.pub;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import com.rpt.util.MailUtil;
import com.synnex.cdc.waf.model.Table;
import com.synnex.cdc.waf.model.User;
import com.synnex.cdc.waf.sql.SQLDAO;
import com.synnex.cdc.waf.util.Format;
import com.synnex.cdc.waf.util.Log;
import com.synnex.cdc.waf.util.ServiceLocator;
import com.synnex.cdc.waf.web.ctrl.ConfigDoc;

public class TPGAGReportDaily {

	private static SQLDAO dbcep;
	private static SQLDAO dbtpgag;

	public static void main(String[] args){
		try{
			String sendTo="walkerh@ndscd.com";//args[0];
			String sendCC="walkerh@ndscd.com";//args[1];
			
			if(args.length>=1){
				sendTo=args[0];
			}
			if(args.length>=2){
				sendCC=args[1];
			}

			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.DAY_OF_MONTH, -1);
			SimpleDateFormat sd = new SimpleDateFormat("MM/dd");
			String rptDate = sd.format(ca.getTime());

			Vector<String> dates = new Vector<String>();
			ca.add(Calendar.DAY_OF_MONTH, -6);
			for(int i=0; i<6; i++){
				dates.add(sd.format(ca.getTime()));
				ca.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			String mon = rptDate.substring(0, 2);
			if(mon.startsWith("0")){
				mon = mon.substring(1);
			}
			
			init();
			//String jobmContent = searchRptDailyJobm(mon,dates,rptDate);
			String tpgagContent = searchRptDailyTpgag(mon,dates,rptDate); 
			sendRptDaily(tpgagContent, rptDate, sendTo, sendCC);
		}catch(Exception e){
			Log.logErr(e);
		}
	}

	private static void init() throws Exception {
		ConfigDoc.init("config/web-config_de.xml");
		Log.init();
		dbtpgag = (SQLDAO) ServiceLocator.getBean("daotpgag");
		dbcep = (SQLDAO) ServiceLocator.getBean("daocep");
	}

	private static String searchRptDailyJobm(String mon, Vector<String> dates, String rptDate)
			throws Exception {
		String sql = "call rpt_daily_jobm()";
		Table table = dbtpgag.search(sql);
		
		StringBuffer content = new StringBuffer();
		content.append("3. PC/スマホ版お仕事検索利用者(独立IP数)");
		
		content.append("<table border=0 cellspacing=1 cellpadding=0 class='tableBody'>");
		content.append("<tr>");
		content.append("<td class='rowHead' rowspan=2 nowrap>項目</td>");
		for(int i=0; i<dates.size(); i++){
			content.append("<td class='rowHead' rowspan=2 nowrap>"+dates.get(i)+"</td>");
		}
		content.append("<td class='rowHead' colspan=6 nowrap>"+rptDate+"</td>");
		content.append("<td class='rowHead' rowspan=2 nowrap>"+mon+"月合計</td>");
		content.append("</tr>");
		content.append("<tr>");
		content.append("<td class='rowHead' nowrap>RD</td>");
		content.append("<td class='rowHead' nowrap>RW</td>");
		content.append("<td class='rowHead' nowrap>RM</td>");
		content.append("<td class='rowHead' nowrap>RM+</td>");
		content.append("<td class='rowHead' nowrap>NU</td>");
		content.append("<td class='rowHead' nowrap>TTL</td>");
		content.append("</tr>");
		
		String classname="class=rowView";
		for (int i = 0; i < table.getRowCount(); i++) {
			classname =" class=rowView"+i%2;
			content.append("<tr>");
			content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "url_desc",true)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d1",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d2",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d3",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d4",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d5",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d6",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+formatByItemCode(table.getCellValue(i, "d7rd",true),null)+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+formatByItemCode(table.getCellValue(i, "d7rw",true),null)+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+formatByItemCode(table.getCellValue(i, "d7rm",true),null)+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+formatByItemCode(table.getCellValue(i, "d7rmp",true),null)+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+formatByItemCode(table.getCellValue(i, "d7uq",true),null)+"</font></td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d7ttl",true),null)+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "dm",true),null)+"</B></td>");
			content.append("</tr>");
		}
		content.append("</table>");
		content.append("<br>");
		return content.toString();
	}
	
	private static String searchRptDailyTpgag(String mon, Vector<String> dates, String rptDate)
			throws Exception {
		String sql = "call rpt_daily_tpgag()";
		Table table = dbtpgag.search(sql);
		
		StringBuffer content = new StringBuffer();
		content.append("Mypage/GPSアプリ利用者(AG数)<br>");
		
		content.append("<table border=0 cellspacing=1 cellpadding=0 class='tableBody'>");
		content.append("<tr>");
		content.append("<td class='rowHead' rowspan=2 nowrap>ユニット名</td>");
		for(int i=0; i<dates.size(); i++){
			content.append("<td class='rowHead' rowspan=2 nowrap>"+dates.get(i)+"</td>");
		}
		content.append("<td class='rowHead' colspan=6 nowrap>"+rptDate+"</td>");
		content.append("<td class='rowHead' rowspan=2 nowrap>"+mon+"月合計</td>");
		content.append("</tr>");
		content.append("<tr>");
		content.append("<td class='rowHead' nowrap>RD</td>");
		content.append("<td class='rowHead' nowrap>RW</td>");
		content.append("<td class='rowHead' nowrap>RM</td>");
		content.append("<td class='rowHead' nowrap>RM+</td>");
		content.append("<td class='rowHead' nowrap>NU</td>");
		content.append("<td class='rowHead' nowrap>TTL</td>");
		content.append("</tr>");
		
		String classname="class=rowView";
		String BStart="";
		String BEnd="";
		for (int i = 0; i < table.getRowCount(); i++) {
			classname =" class=rowView"+i%2;
			if(i==table.getRowCount()-1){
				BStart = "<B>";
				BEnd = "</B>";
			}
			content.append("<tr>");
			content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "dept_desc",true)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d1",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d2",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d3",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d4",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d5",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d6",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+BStart+formatByItemCode(table.getCellValue(i, "d7rd",true),null)+BEnd+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+BStart+formatByItemCode(table.getCellValue(i, "d7rw",true),null)+BEnd+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+BStart+formatByItemCode(table.getCellValue(i, "d7rm",true),null)+BEnd+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+BStart+formatByItemCode(table.getCellValue(i, "d7rmp",true),null)+BEnd+"</font></td>");
			content.append("<td "+classname+" align=right nowrap><font color=green>"+BStart+formatByItemCode(table.getCellValue(i, "d7uq",true),null)+BEnd+"</font></td>");
			content.append("<td "+classname+" align=right nowrap>"+BStart+formatByItemCode(table.getCellValue(i, "d7ttl",true),null)+BEnd+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "dm",true),null)+"</B></td>");
			content.append("</tr>");
		}
		content.append("</table>");
		content.append("RD：リピータユーザー、最近 2日間以内数回利用。");
		content.append("<br>RW：リピータユーザー、最近1週間以内数回利用。");
		content.append("<br>RM：リピータユーザー、最近1ヵ月以内数回利用。");
		content.append("<br>RM+：リピータユーザー、1ヵ月前に数回利用。");
		content.append("<br>NU：新規ユーザー。");
		return content.toString();
	}
	
	
	public static String formatByItemCode(String s, String itemCode) {
		String rs = s;
		if(rs==null || rs.equals("") || rs.equals("0")){
			rs = "";
		}else if(itemCode!=null && itemCode.startsWith("unit_")){
			rs = Format.getNumber(rs, 2);
		}else if (itemCode!=null && itemCode.endsWith("_rate")){
			rs = Format.getPercent(rs);
		}else{
			rs = Format.getNumber(rs, 0);
		}
		if(rs.startsWith("-")){
			rs = "<font color=red>"+rs+"</font>";
		}
		return rs;
	}

	private static void sendRptDaily(String tpgagContent, String rptDate, String sendTo, String sendCC) throws Exception {
		String subject = "Mypage/GPSアプリ利用者 - " + rptDate;

		StringBuffer content = new StringBuffer();
		content.append("<style>");
		content.append("body {FONT-FAMILY: Arial; FONT-SIZE: 9pt;}");
		content.append(".tableBody {BACKGROUND:#18618A;border: 1 solid #18618A;}");
		content
		.append(".rowHead  {padding-left: 5; padding-right: 5; font-family: Arial;font-weight:bold; font-size: 8pt; color: #000000; BACKGROUND: #BDCFCF; HEIGHT: 20px;text-align: center;}");
		content
				.append(".rowView0  {padding-left: 5; padding-right: 5; font-family: Arial;  font-size: 8pt; color: #000000; BACKGROUND: #FFFFFF; HEIGHT: 18px;}");
		content
				.append(".rowView1  {padding-left: 5; padding-right: 5; font-family: Arial;  font-size: 8pt; color: #000000; BACKGROUND: #EBF3F3; HEIGHT: 18px;}");
		content.append("</style>");

		content.append("<br>");
		content.append(tpgagContent);
		//content.append("<br>");
		//content.append(jobmContent);

		MailUtil.getInstanse().sendMailN(sendTo, sendCC, null, subject, content.toString());
	}

}
