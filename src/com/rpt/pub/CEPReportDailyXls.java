package com.rpt.pub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

import com.rpt.util.MailUtil;
import com.synnex.cdc.waf.model.Table;
import com.synnex.cdc.waf.model.User;
import com.synnex.cdc.waf.sql.SQLDAO;
import com.synnex.cdc.waf.util.Format;
import com.synnex.cdc.waf.util.Log;
import com.synnex.cdc.waf.util.ServiceLocator;
import com.synnex.cdc.waf.web.ctrl.ConfigDoc;

public class CEPReportDailyXls {

	private static SQLDAO dbcep;
	private static SQLDAO dbtpgag;
	
	private static String rptDate1;
	
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

			init();
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.DAY_OF_MONTH, -1);
			SimpleDateFormat sd = new SimpleDateFormat("MM/dd");
			SimpleDateFormat sd1 = new SimpleDateFormat("MM-dd");
			String rptDate = sd.format(ca.getTime());
			rptDate1 = sd1.format(ca.getTime());
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
			File file = searchRptDailyCep(mon,dates,rptDate);
			ArrayList<File> files = new ArrayList<File>();
			files.add(file);
			sendRptDaily(rptDate, sendTo, sendCC, files);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void init() throws Exception {
		ConfigDoc.init("config/web-config_de.xml");
		Log.init();
		dbtpgag = (SQLDAO) ServiceLocator.getBean("daotpgag");
		dbcep = (SQLDAO) ServiceLocator.getBean("daocep");
	}
	
	@SuppressWarnings("static-access")
	private static File searchRptDailyCep(String mon, Vector<String> dates, String rptDate)
			throws Exception {
		String sql = "call rpt_daily_cep()";
		Table table = dbcep.search(sql);
		User user = new User();
		user.setVisiblerate(1);
		//打包发附件
		HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("データ入力");
        workbook.setSheetName(0, "データ入力"+rptDate1, workbook.ENCODING_UTF_16);
        //列宽
        sheet.setColumnWidth((short) 0, (short)4000);
        sheet.setColumnWidth((short) 2, (short)2500);
        sheet.setColumnWidth((short) 3, (short)2500);
        sheet.setColumnWidth((short) 4, (short)2500);
        sheet.setColumnWidth((short) 5, (short)2500);
        sheet.setColumnWidth((short) 6, (short)2500);
        sheet.setColumnWidth((short) 7, (short)2500);
        sheet.setColumnWidth((short) 8, (short)2500);
        sheet.setColumnWidth((short) 9, (short)2500);
        //header 样式
        HSSFCellStyle header = workbook.createCellStyle();
        header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short)10);
        header.setFont(font);
        
        //设置边框
        header.setBorderBottom((short) 1);   
        header.setBorderTop((short) 1);
        header.setBorderLeft((short) 1);
        header.setBorderRight((short) 1); 
        
        HSSFCellStyle normal = workbook.createCellStyle();
        normal.setBorderBottom((short) 1);   
        normal.setBorderTop((short) 1);
        normal.setBorderLeft((short) 1);
        normal.setBorderRight((short) 1); 
        normal.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//第一行
		HSSFRow row0 = sheet.createRow(0);
		HSSFCell cell0 = row0.createCell((short) 0);
		cell0.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell0.setEncoding(HSSFCell.ENCODING_UTF_16);
		cell0.setCellStyle(header);
        cell0.setCellValue(new String("項目"));
        HSSFCell cell1 = row0.createCell((short) 1);
        cell1.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell1.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell1.setCellStyle(header);
        cell1.setCellValue(new String("明細"));
        for(int i=0; i<dates.size(); i++){
        	HSSFCell cell = row0.createCell((short) (i+2));
        	cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        	cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        	cell.setCellStyle(header);
        	cell.setCellValue(dates.get(i));
        }
        HSSFCell cell3 = row0.createCell((short) (dates.size()+2));
        cell3.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell3.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell3.setCellStyle(header);
        cell3.setCellValue(rptDate);
        HSSFCell cell4 = row0.createCell((short) (dates.size()+3));
        cell4.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell4.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell4.setCellStyle(header);
        cell4.setCellValue(mon+"月合計");

		String itemCode;
		String itemDesc1;
		
		for (int i = 0; i < table.getRowCount(); i++) {
			itemCode = table.getCellValue(i, "item_code",true);
			itemDesc1 = table.getCellValue(i, "item_desc1",true);
			HSSFRow row = sheet.createRow(i+1);
			if(itemCode.startsWith("hc_")){
				if(!itemDesc1.equals("")){
					HSSFCell cell = row.createCell((short) 0); 
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setEncoding(HSSFCell.ENCODING_UTF_16);
					cell.setCellStyle(normal);
					sheet.addMergedRegion(new Region(i+1, (short)0, i+2, (short)0));
					cell.setCellValue(itemDesc1);
				}
			}else if(itemCode.startsWith("unit_cost_") || itemCode.startsWith("cost_")){
				if(!itemDesc1.equals("")){
					HSSFCell cell = row.createCell((short) 0); 
					sheet.addMergedRegion(new Region(i+1, (short)0, i+4, (short)0));
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setEncoding(HSSFCell.ENCODING_UTF_16);
					cell.setCellStyle(normal);
					cell.setCellValue(itemDesc1);
				}
			}else{
				HSSFCell cell = row.createCell((short) 0); 
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setEncoding(HSSFCell.ENCODING_UTF_16);
				cell.setCellStyle(normal);
				cell.setCellValue(itemDesc1);
			}
			HSSFCell cell11 = row.createCell((short) 1);
			cell11.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell11.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell11.setCellStyle(normal);
			cell11.setCellValue(table.getCellValue(i, "item_desc2",true));
			
			formatByItemCode(workbook,row,(short) 2,table.getCellValue(i, "d1",true),itemCode);
			formatByItemCode(workbook,row,(short) 3,table.getCellValue(i, "d2",true),itemCode);
			formatByItemCode(workbook,row,(short) 4,table.getCellValue(i, "d3",true),itemCode);
			formatByItemCode(workbook,row,(short) 5,table.getCellValue(i, "d4",true),itemCode);
			formatByItemCode(workbook,row,(short) 6,table.getCellValue(i, "d5",true),itemCode);
			formatByItemCode(workbook,row,(short) 7,table.getCellValue(i, "d6",true),itemCode);
			formatByItemCode(workbook,row,(short) 8,table.getCellValue(i, "d7",true),itemCode);
			formatByItemCode(workbook,row,(short) 9,table.getCellValue(i, "dm",true),itemCode);
		}
		//行尾
		int footRownumber = sheet.getLastRowNum();    
	    HSSFRow footRow = sheet.createRow(footRownumber + 1);    
	    HSSFCell footRowcell = footRow.createCell((short) 0); 
	    footRowcell.setCellType(HSSFCell.CELL_TYPE_STRING);
	    footRowcell.setEncoding(HSSFCell.ENCODING_UTF_16);
	    footRowcell.setCellStyle(normal);
	    sheet.addMergedRegion(new Region(footRownumber+1, (short)0, footRownumber+2, (short)9));
	    footRowcell.setCellValue(new String("同原価にはシステム以外で作業した費用が含まれていません。"));
		File file = new File("xls/データ入力 "+rptDate1+".xls");  
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.flush();
        fileOut.close();
		return file;
	}
	
	public static HSSFCell formatByItemCode(HSSFWorkbook workbook,HSSFRow row,short num, String s, String itemCode) {
		HSSFCellStyle rightCellStyle;
		HSSFFont redFont;
		rightCellStyle = workbook.createCellStyle();
        rightCellStyle.setBorderBottom((short) 1);   
        rightCellStyle.setBorderTop((short) 1);
        rightCellStyle.setBorderLeft((short) 1);
        rightCellStyle.setBorderRight((short) 1);
        rightCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

        redFont = workbook.createFont();
        redFont.setColor(HSSFFont.COLOR_RED);
        
		HSSFCell cell = row.createCell((short) num); 
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
		String rs = s;
		if(rs==null || rs.equals("") || rs.equals("0")){
			rs = "";
			cell.setCellStyle(rightCellStyle);
			cell.setCellValue(rs);
		}else if(itemCode!=null && itemCode.startsWith("unit_")){
			rs = Format.getNumber(rs, 2);
			cell.setCellStyle(rightCellStyle);
			cell.setCellValue(rs);
		}else if (itemCode!=null && itemCode.endsWith("_rate")){
			rs = Format.getPercent(rs);
			cell.setCellStyle(rightCellStyle);
			cell.setCellValue(rs);
		}else{
			rs = Format.getNumber(rs, 0);
			cell.setCellStyle(rightCellStyle);
			cell.setCellValue(rs);
		}
		if(rs.startsWith("-")){
			rightCellStyle.setFont(redFont);
			cell.setCellStyle(rightCellStyle);
			cell.setCellValue(rs);
		}
		return cell;
	}

	private static void sendRptDaily(String rptDate, String sendTo, String sendCC, ArrayList<File> files) throws Exception {
		String subject = "Cep Daily report for Finance - " + rptDate;

		StringBuffer content = new StringBuffer();
		content.append(subject);
		sendMail(sendTo, sendCC, null, subject, content.toString(), files);
	}

	public static void sendMail(String sendTo, String sendCC, String sendBCC,
			String subject, String content,ArrayList<File> attachments) throws Exception {
		try {
			Properties mailP = new Properties();
			InputStream in = MailUtil.class.getResourceAsStream("/mail.properties");
			try {
				mailP.load(in);
			} catch (IOException e) {
			}
			String sendFrom = mailP.getProperty("mail.smtp.from");
			Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " cc:" + sendCC + " subject:"
					+ subject);
			Properties props = System.getProperties();
			props.put("mail.smtp.host", mailP.getProperty("mail.smtp.host"));
			props.put("mail.smtp.port", mailP.getProperty("mail.smtp.port"));
			Session session;
			session = Session.getDefaultInstance(props, null);
			// session.setDebug(true);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sendFrom));

			InternetAddress[] address = InternetAddress.parse(sendTo);
			MimeMultipart multi = new MimeMultipart();
			BodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(
					"<meta http-equiv=Content-Type content=text/html;charset=utf-8>"
							+ content, "text/html;charset=utf-8");
			multi.addBodyPart(textBodyPart);
			msg.setRecipients(Message.RecipientType.TO, address);

			if (sendCC != null && !sendCC.trim().equals("")) {
				InternetAddress[] addressCC = InternetAddress.parse(sendCC);
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			
			if (sendBCC != null && !sendBCC.trim().equals("")) {
				InternetAddress[] addressBCC = InternetAddress.parse(sendBCC);
				msg.setRecipients(Message.RecipientType.BCC, addressBCC);
			}
			// 添加邮件附件
			for (File file : attachments) {
				BodyPart attachmentBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(file);
				attachmentBodyPart.setDataHandler(new DataHandler(source));
				attachmentBodyPart.setFileName(MimeUtility.encodeWord(file
						.getName()));
				multi.addBodyPart(attachmentBodyPart);
			}
			msg.setSubject(subject);
			msg.setContent(multi);
			Transport.send(msg);

			Log.log("SendMail:from:" + sendFrom + " to:" + sendTo + " successed.");
		} catch (MessagingException mex) {
			mex.printStackTrace();
			Log.logErr(mex);
		}
	}
}
