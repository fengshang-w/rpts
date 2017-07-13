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

public class CEPReportDailyN {

	private static SQLDAO dbcep;

	public static void main(String[] args){
		try{
			String sendTo="konatsu.chou@threepro.co.jp";//args[0];
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
			int dIdx = ca.get(Calendar.DAY_OF_MONTH);
			ca.add(Calendar.DAY_OF_MONTH, 1-dIdx);
			for(int i=0; i<dIdx; i++){
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
		String sql = "call rpt_daily_cep_n()";
		Table table = dbcep.search(sql);
		User user = new User();
		user.setVisiblerate(1);
		
		StringBuffer content = new StringBuffer();
		
		content.append("<table border=0 cellspacing=1 cellpadding=0 class='tableBody'>");
		content.append("<tr>");
		content.append("<td class='rowHead' nowrap>項目</td>");
		content.append("<td class='rowHead' colspan=2 nowrap>明細</td>");
		for(int i=0; i<dates.size(); i++){
			content.append("<td class='rowHead' nowrap>"+dates.get(i)+"</td>");
		}
		content.append("<td class='rowHead' nowrap>"+mon+"月<br>ＣＥＰ小計</td>");
		content.append("<td class='rowHead' nowrap>CEP以外の<br>入力業務等</td>");
		content.append("<td class='rowHead' nowrap>"+mon+"月経過分小計</td>");
		content.append("<td class='rowHead' nowrap>CEP<br>見込残</td>");
		content.append("<td class='rowHead' nowrap>ＣＥＰ以外の<br>入力案件<br>見込残</td>");
		content.append("<td class='rowHead' nowrap>"+mon+"月<br>未経過小計</td>");
		content.append("<td class='rowHead' nowrap>従来の月で<br>発生しない<br>収益・費用等</td>");
		content.append("<td class='rowHead' nowrap>"+mon+"月<br>合計</td>");
		content.append("</tr>");

		String classname="class=rowView";
		String itemCode;
		String itemDesc1;
		String itemDesc2;
		String itemDesc3;

		for (int i = 0; i < table.getRowCount(); i++) {
			itemCode = table.getCellValue(i, "item_code",true);
			itemDesc1 = table.getCellValue(i, "item_desc1",true);
			itemDesc2 = table.getCellValue(i, "item_desc2",true);
			itemDesc3 = table.getCellValue(i, "item_desc3",true);
			classname =" class=rowView"+i%2;
			content.append("<tr>");
			
			if(itemCode.equals("deliver_j_count")){
				content.append("<td "+classname+" rowspan=5 nowrap>"+itemDesc1+"</td>");
			}else if(itemCode.equals("hc_jp")){
				content.append("<td "+classname+" rowspan=12 nowrap>"+itemDesc1+"</td>");
			}else if(itemCode.equals("gross_margin")){
				content.append("<td "+classname+" rowspan=2 nowrap>"+itemDesc1+"</td>");
			}else if(itemCode.equals("cost_overhead_hr")){
				content.append("<td "+classname+" rowspan=3 nowrap>"+itemDesc1+"</td>");
			}else if(itemCode.equals("net_profit")){
				content.append("<td "+classname+" rowspan=2 nowrap>"+itemDesc1+"</td>");
			}
			
			if(itemCode.equals("hc_jp")){
				content.append("<td "+classname+" rowspan=2 nowrap>"+itemDesc2+"</td>");
			}else if(itemCode.equals("cost_entry")){
				content.append("<td "+classname+" rowspan=4 nowrap>"+itemDesc2+"</td>");
			}else if(itemCode.equals("cost2_cep")){
				content.append("<td "+classname+" rowspan=4 nowrap>"+itemDesc2+"</td>");
			}else if(!itemDesc2.equals("")){
				content.append("<td "+classname+" nowrap>"+itemDesc2+"</td>");
			}
			
			content.append("<td "+classname+" nowrap>"+itemDesc3+"</td>");
			
			for(int j=1; j<=dates.size(); j++){
				content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "d"+j,true),itemCode)+"</td>");
			}
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "dm",true),itemCode)+"</B></td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "dm_noncep",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "dms",true),itemCode)+"</B></td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "remain_cep",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "remain_noncep",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "remain_total",true),itemCode)+"</B></td>");
			content.append("<td "+classname+" align=right nowrap>"+formatByItemCode(table.getCellValue(i, "special",true),itemCode)+"</td>");
			content.append("<td "+classname+" align=right nowrap><B>"+formatByItemCode(table.getCellValue(i, "monttl",true),itemCode)+"</B></td>");
			
			content.append("</tr>");
		}
		content.append("</table>");
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
		String subject = "CEP Daily report - " + rptDate;

		StringBuffer content = new StringBuffer();
		content.append(cepContent);

		MailUtil.getInstanse().sendMailN(sendTo, sendCC, null, subject, content.toString());
	}

}
