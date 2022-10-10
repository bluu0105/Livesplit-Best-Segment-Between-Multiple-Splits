import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMsplitsparser {
    
    //Will take in strings of format XX:XX:XX.XXXXXXX
    public static double DoublifyString(String num) {
        Double minutes = 60 * Double.parseDouble(num.substring(3, 5));
        Double seconds = Double.parseDouble(num.substring(6, 8));
        Double decimals = Double.parseDouble("0." + num.substring(9, 16));
        return minutes + seconds + decimals;
    }

    /*Will take in a HashMap consisting of Strings keys representing attempts and Double values
     * representing the overall segment time for that specific attempt. The minimum value is
     * found and returned in a String of form "XXm | XXs | 0.XXXms"
    */
    public static String fastestTime(HashMap<String, Double> map) {
        double fastestValue = 3540;
        for (String key : map.keySet()) {
            if (map.get(key) < fastestValue) {
                fastestValue = map.get(key);
            }
        }
        Integer intDecimals = (((Double)(fastestValue * 1000)).intValue()) % 1000;
        Double doubleDecimals = (intDecimals.doubleValue()) / 1000;
        String doubleDecimalsSTRING = Double.toString(doubleDecimals);

        Integer intMinSec = ((Double)Math.floor(fastestValue)).intValue();
        Integer intMin = intMinSec / 60;
        Integer intSec = intMinSec % 60;
      
        //return intMin + "m | " + intSec + "s | " + doubleDecimalsSTRING.substring(2) + "ms";
        return intMin + ":" + intSec + "." + doubleDecimalsSTRING.substring(2);
    }

    public static void main(String[] args) {

        String inBetweenSegmentName = "Boots";    //INSERT INBETWEEN SEGMENT HERE
        NodeList inBetweenSegmentList = null;
        HashMap<String, String> inBetweenMap = new HashMap<>();

        String endSegmentName = "Enter Faron";    //INSERT END SEGMENT HERE
        NodeList endSegmentList = null;
        HashMap<String, String> endMap = new HashMap<>();

        File lssFile = new File("TP_test.lss");    //INSERT FILENAME HERE

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(lssFile);
            NodeList segmentList = doc.getElementsByTagName("Segment");    //A list of all segment tags
            for (int i = 0; i < segmentList.getLength(); i++) {
                Node segment = segmentList.item(i);
                NodeList segmentInfoList = segment.getChildNodes();    //A list of all child info tags for each segment
                boolean inBetweenSegMatch = false;
                boolean endSegMatch = false;
                for (int j = 0; j < segmentInfoList.getLength(); j++) {

                    String infoName = segmentInfoList.item(j).getNodeName();
                    if (infoName == "Name") {
                        String segName = segmentInfoList.item(j).getFirstChild().getNodeValue();
                        if (segName.equals(inBetweenSegmentName)) {    //If the segment name matches the segment we want, instantiate the Segment List
                            inBetweenSegMatch = true;
                        }
                        if (segName.equals(endSegmentName)) {
                            endSegMatch = true;
                        }
                    }
                    if (infoName == "SegmentHistory" && endSegMatch) {
                        System.out.println("For: " + endSegmentName);
                        NodeList attemptList = segmentInfoList.item(j).getChildNodes();    //A list of every attempt that reached this segment
                        
                        for (int k = 0; k < attemptList.getLength(); k++) {
                            if (attemptList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element attempt = (Element) attemptList.item(k);
                                String attemptNumKey = attempt.getAttribute("id");
                                NodeList timeList = attempt.getChildNodes();    //A list of all times corresponding to each segment in attemptList
                                for (int m = 0; m < timeList.getLength(); m++) {
                                    if (timeList.item(m).getNodeType() == Node.ELEMENT_NODE) {
                                        Element time = (Element) timeList.item(m);
                                        String timeVal = time.getFirstChild().getNodeValue();
                                        //System.out.println(attemptNumKey + " ::: " + timeVal);    //Uncomment to see all attempt segments
                                        endMap.put(attemptNumKey, timeVal);
                                    }
                                }
                            }
                        }
                        endSegMatch = false;
                    }
                    if (infoName == "SegmentHistory" && inBetweenSegMatch) {
                        System.out.println("For: " + inBetweenSegmentName);
                        NodeList attemptList = segmentInfoList.item(j).getChildNodes();    //A list of every attempt that reached this segment
                        
                        for (int k = 0; k < attemptList.getLength(); k++) {
                            if (attemptList.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                Element attempt = (Element) attemptList.item(k);
                                String attemptNumKey = attempt.getAttribute("id");
                                NodeList timeList = attempt.getChildNodes();    //A list of all times corresponding to each segment in attemptList
                                for (int m = 0; m < timeList.getLength(); m++) {
                                    if (timeList.item(m).getNodeType() == Node.ELEMENT_NODE) {
                                        Element time = (Element) timeList.item(m);
                                        String timeVal = time.getFirstChild().getNodeValue();
                                        //System.out.println(attemptNumKey + " ::: " + timeVal);    //Uncomment to see all attempt segments
                                        inBetweenMap.put(attemptNumKey, timeVal);
                                    }
                                }
                            }
                        }
                        inBetweenSegMatch = false;
                    }

                }
        
            }

            HashMap<String, Double> combinedMap = new HashMap<>(); //For loop will check if endMap and inBetweenMap contain the same key and will add that attempt and the value of adding the 2 segments into a new key value pair
            for (String key : endMap.keySet()) {
                if (inBetweenMap.containsKey(key)) {
                    double sumOfTime = DoublifyString(endMap.get(key)) + DoublifyString(inBetweenMap.get(key));
                    combinedMap.put(key, sumOfTime);
                }
            }
            System.out.println();
            System.out.println(fastestTime(combinedMap));

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
