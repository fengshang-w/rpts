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

public class CEPReportDaily {

	private static SQLDAO dbcep;

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
			String cepContent = searchRptDailyCep(mon,dates,rptDate);
			sendRptDaily(cepContent, rptDate, sendTo, sendCC);
		}catch(Exception e){
			Log.logErr(e);
		}
	}

	private static void init() throws Exception {
		ConfigDoc.init("config/web-config_de.xml");
		Log.init();
		dbcep = (SQLDAO) ServiceLocator.getBean("daocep");
	}

	private static String searchRptDailyCep(String mon, Vector<String> dates, String rptDate)
			throws Exception {
		String sql = "call rpt_daily_cep()";
		Table table = dbcep.search(sql);
		User user = new User();
		user.setVisiblerate(1);
		
		StringBuffer content = new StringBuffer();
		content.append("1.データ入力");
		
		content.append("<table border=0 cellspacing=1 cellpadding=0 class='tableBody'>");
		content.append("<tr>");
		content.append("<td class='rowHead' nowrap>項目</td>");
		content.append("<td class='rowHead' nowrap>明細</td>");
		for(int i=0; i<dates.size(); i++){
			content.append("<td class='rowHead' nowrap>"+dates.get(i)+"</td>");
		}
		content.append("<td class='rowHead' nowrap>"+rptDate+"</td>");
		content.append("<td class='rowHead' nowrap>"+mon+"月合計</td>");
		content.append("</tr>");

		String classname="class=rowView";
		String itemCode;
		String itemDesc1;

		for (int i = 0; i < table.getRowCount(); i++) {
			itemCode = table.getCellValue(i, "item_code",true);
			itemDesc1 = table.getCellValue(i, "item_desc1",true);
			classname =" class=rowView"+i%2;
			content.append("<tr>");
			if(itemCode.startsWith("hc_")){
				if(!itemDesc1.equals("")){
					content.append("<td "+classname+" rowspan=2 nowrap>"+itemDesc1+"</td>");
				}
			}else if(itemCode.startsWith("unit_cost_") || itemCode.startsWith("cost_")){
				if(!itemDesc1.equals("")){
					content.append("<td "+classname+" rowspan=4 nowrap>"+itemDesc1+"</td>");
				}
			}else{
				content.append("<td "+classname+" nowrap>"+itemDesc1+"</td>");
			}
			content.append("<td "+classname+" nowrap>"+table.getCellValue(i, "item_desc2",true)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d1",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d2",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d3",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d4",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d5",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d6",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d7",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "dm",true),itemCode)+"</B></td>");
			content.append("</tr>");
		}
		content.append("</table>");
		content.append("同原価にはシステム以外で作業した費用が含まれていません。");
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

	private static void sendRptDaily(String cepContent, String rptDate, String sendTo, String sendCC) throws Exception {
		String subject = "Daily report for Data Entry - " + rptDate;

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
		content.append(cepContent);
		content.append("<br>");

		MailUtil.getInstanse().sendMailN(sendTo, sendCC, null, subject, content.toString());
	}

}
