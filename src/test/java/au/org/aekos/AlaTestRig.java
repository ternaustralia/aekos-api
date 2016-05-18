package au.org.aekos;

import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

public class AlaTestRig {
	public static void main(String[] args) {
		new AlaTestRig().run();
	}

	private void run() {
		RestTemplate rt = new RestTemplate();
		String result = rt.getForObject("http://bie.ala.org.au/ws/search.json?q=Leersia%20hexandra", String.class);
		JSONObject obj = new JSONObject(result);
		JSONObject firstResult = obj.getJSONObject("searchResults").getJSONArray("results").getJSONObject(1);
		System.out.println(firstResult);
	}
}
