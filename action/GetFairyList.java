package action;

import info.FairyBattleInfo;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData;
import walker.Info;
import walker.Process;
import action.ActionRegistry.Action;

public class GetFairyList {
	public static final Action Name = Action.GET_FAIRY_LIST;
	//TODO:妖精列表更正
	private static final String URL_FAIRY_LIST = "http://game1-cbt.ma.sdo.com:10001/connect/app/exploration/fairy_floor?cyt=1";
	
	private static byte[] response;
	
	public static boolean run() throws Exception {
		try {
			response = Process.network.ConnectToServer(URL_FAIRY_LIST, new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.text;
			ErrorData.currentErrorType = ErrorData.ErrorType.ConnectionError;
			ErrorData.text = ex.getMessage();
			throw ex;
		}

		Document doc;
		try {
			doc = Process.ParseXMLBytes(response);
		} catch (Exception ex) {
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyListDataError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		try {
			return parse(doc);
		} catch (Exception ex) {
			throw ex;
		}
		
	}
	private static boolean parse(Document doc) throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				ErrorData.currentErrorType = ErrorData.ErrorType.FairyListResponse;
				ErrorData.currentDataType = ErrorData.DataType.text;
				ErrorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			if (!xpath.evaluate("//remaining_rewards", doc).equals("0")) {
				Process.info.events.push(Info.EventType.fairyReward);
			}
			
			// TODO: 这两周先是只寻找0BC的，之后再扩展
			//NodeList fairy = (NodeList)xpath.evaluate("//fairy_select/fairy_event[put_down=4]/fairy", doc, XPathConstants.NODESET);
			NodeList fairy = (NodeList)xpath.evaluate("//fairy_select/fairy_event[put_down=1]/fairy", doc, XPathConstants.NODESET);
			
			ArrayList<FairyBattleInfo> fbis = new ArrayList<FairyBattleInfo>();
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				FairyBattleInfo fbi = new FairyBattleInfo();
				boolean attack_flag = false;
				do {
					if (f.getNodeName().equals("serial_id")) {
						fbi.SerialId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("discoverer_id")) {
						fbi.UserId = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("lv")) {
						fbi.FairyLevel = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("name")) {
						fbi.FairyName = f.getFirstChild().getNodeValue();
					} else if (f.getNodeName().equals("rare_flg")) {
						if (f.getFirstChild().getNodeValue().equals("1")) {
							fbi.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.RARE;
						} else {
							fbi.Type = FairyBattleInfo.PRIVATE;
						}
					}
					f = f.getNextSibling();
				} while (f != null);
				if (Info.AllowAttackSameFairy) {
					fbis.add(fbi);
				} else {
					for (FairyBattleInfo bi : Process.info.LatestFairyList) {
						if (bi.equals(fbi)) {
							// 已经舔过
							attack_flag = true;
							break;
						}
					}
					if (!attack_flag) fbis.add(fbi);
				}	
			}
			
			
			if (fbis.size() > 1) Process.info.events.push(Info.EventType.fairyAppear); // 以便再次寻找
			if (fbis.size() > 0) {
				Process.info.events.push(Info.EventType.fairyCanBattle);
				Process.info.fairy = fbis.get(0);
			}
			
			Process.info.SetTimeoutByAction(Name);
			
		} catch (Exception ex) {
			if (ErrorData.currentErrorType != ErrorData.ErrorType.none) throw ex;
			ErrorData.currentDataType = ErrorData.DataType.bytes;
			ErrorData.currentErrorType = ErrorData.ErrorType.FairyListDataParseError;
			ErrorData.bytes = response;
			throw ex;
		}
		
		return true;

	}
}
