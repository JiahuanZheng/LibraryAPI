import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Login {
	public int login(String loginName, String password, String bookName,
			String pickPlace) {
		String url = "http://202.120.227.11/F?func=login-session&bor_library=FDU50&"
				+ "login_source=bor-info&bor_id="
				+ loginName
				+ "&bor_verification=" + password;
		System.out.println(url);

		Document doc;
		try {

			// get the retrieval page url.
			doc = Jsoup.connect(url).get();
			Element element = doc.getElementById("header");
			Elements elements = element.children();
			Element element2 = elements.get(1);
			String href = element2.attr("href");
			System.out.println(href);

			// get the query action url.
			doc = Jsoup.connect(href).get();
			Elements elements2 = doc.getElementsByAttribute("action");
			Element element3 = elements2.get(0);
			String actionHref = element3.attr("action");

			String queryActionTail = "?func=find-b&adjacent=Y&find_code=WRD&request="
					+ bookName
					+ "&local_base=FDU01&filter_code_1=WLN&filter_request_1="
					+ "&filter_code_2=WYR&filter_request_2=&filter_code_3=WYR&filter_request_3="
					+ "&filter_code_4=WFM&filter_request_4=&filter_code_5=WSL&filter_request_5=";

			actionHref += queryActionTail;
			System.out.println(actionHref);

			doc = Jsoup.connect(actionHref).get();
			Elements elements3 = doc.getElementsByAttributeValue("class",
					"items");
			String str = null;
			if (elements3.size() != 0) {
				int i = 0;
				for (Element element4 : elements3) {
					Element element5 = element4.getElementsByAttribute(
							"onmouseover").get(0);
					System.out.println("abc"+ element5.text());

					int total = element5.text().charAt(6);
					int notAvailable = element5.text().charAt(15);
					if (total > notAvailable) {
						String strParser = element5.attr("onmouseover");
						// System.out.println(strParser);
						int subIndex1 = strParser.indexOf("http://");
						// System.out.println(subIndex1);
						int subIndex2 = strParser.indexOf(">", subIndex1);
						str = strParser.substring(subIndex1, subIndex2);
						System.out.println("heheh"+str);
						if( getAvailableBookList(str,pickPlace)!= -1 || (i++)>= elements3.size()) break;
					}
				}

			} else
				return -1;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;

	}

	public static void main(String[] args) {
		if (new Login().login("13110240003", "1111", "Jane+Eyre", "ZJGU") == -1)
			System.out.println("Execute Wrong");
	}

	public int getAvailableBookList(String queryStr,String pickPlace) {

		Document doc;
		if (queryStr != null) { // Now, str is a string of book list associated
			// with book query name.
			try {
				doc = Jsoup.connect(queryStr).get();
 
			Element eles = doc.select("body > center > table").get(5);
			Elements eless = eles.select("tbody > tr");

			String urlBooking = null;
			int i;
			for (i = 1; i < eless.size(); i++) {
				Elements eles2 = eless.get(i).select("td");
				if (eles2.get(2).text().contains("在架上")) {
					Element ele = eles2.get(5);
					urlBooking = ele.child(0).attr("href");
					System.out.println("在架子上"+urlBooking);
					break;
				}
			}
			if (i == eless.size())
				return -1;

			doc = Jsoup.connect(urlBooking).get();

			Elements elements5 = doc.getElementsByAttribute("action");
			Element element6 = elements5.get(0);
			String request = element6.attr("action");

			String adm_doc_number = element6.select("input").get(2)
					.attr("value");
			String item_sequence = element6.select("input").get(3)
					.attr("value");

			Element element4 = element6.select("table > tbody > tr").get(2);
			String from = element4.select("td").get(1).select("input").get(0)
					.attr("value");
			String to = element4.select("td").get(1).select("input").get(1)
					.attr("value");

			lastStep(request, adm_doc_number, item_sequence, pickPlace, from,
					to);

			System.out.println("request" + request + "\n" + "adm_doc_number"
					+ adm_doc_number + "\n" + "item_sequence" + item_sequence
					+ "\n" + "pickPlace" + pickPlace + "\n" + "from" + from
					+ "\n" + "to" + to + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	void lastStep(String request, String adm_doc_number, String item_sequence,
			String PICKUP, String from, String to) {
		String urlParameters = "func=item-hold-request-b&doc_library=FDU50&"
				+ "adm_doc_number=" + adm_doc_number + "&item_sequence="
				+ item_sequence + "&bib_request=N&" + "PICKUP=" + PICKUP
				+ "&from=" + from + "&to=" + to + "&x=40&y=8";
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		// String request =
		// "http://202.120.227.11:80/F/2N85A3SRFRQ73A1DFYAHBP6L8AUS5M9PJ6K75GCXKF55I7JHHE-48330";
		URL url;
		try {
			url = new URL(request);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length",
					Integer.toString(postDataLength));
			conn.setUseCaches(false);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postData);
			conn.getInputStream();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Msg " + e);
		}

	}

}
