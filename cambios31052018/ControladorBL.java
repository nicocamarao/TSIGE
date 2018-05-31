import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ControladorBL {

	private static ControladorBL instance = null;
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String SERVER_IP = "18.231.190.192";
	private static final String SERVER_PORT = "9080";
	private static final String GET_CONTENEDORES_URL_1 = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Contenedor')&$expand=Datastreams($select=id)&$select=id,Datastreams&$top=200&$skip=0";
	private static final String GET_CONTENEDORES_URL_2 = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Contenedor')&$expand=Datastreams($select=id)&$select=id,Datastreams&$top=200&$skip=200";
	private static final String GET_CAMIONES_URL = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,%27Camion%27)&$select=id&$top=500";

	private HashMap<Integer, MiContenedor> contenedores;
	private HashMap<Integer, Camion> camiones;

	private ControladorBL() {
		this.contenedores = new HashMap<Integer, MiContenedor>();
		this.camiones = new HashMap<Integer, Camion>();

		loadContenedoresFromServer(GET_CONTENEDORES_URL_1);
		loadContenedoresFromServer(GET_CONTENEDORES_URL_2);
		loadCamionesFromServer(GET_CAMIONES_URL);
	}

	public static ControladorBL getInstance() {
		if (instance == null) {
			instance = new ControladorBL();
		}
		return instance;
	}

	public HashMap<Integer, MiContenedor> getContenedores() {
		return this.contenedores;
	}

	public HashMap<Integer, Camion> getCamiones() {
		return this.camiones;
	}

	public MiContenedor getContenedor(Integer id) {
		try {
			return contenedores.get(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Camion getCamion(Integer id) {
		try {
			return camiones.get(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setEstado(Long id, String estado) {
		try {
			if (contenedores.get(id) != null)
				contenedores.get(id).setEstado(estado);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Double getCapacidad (Long id){
		try {
			if (contenedores.get(id) != null)
				return contenedores.get(id).getCapacidad();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0;
	}
	
	public String getBarrio (Long id){
		try {
			if (contenedores.get(id) != null)
				return contenedores.get(id).getBarrio();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void loadContenedoresFromServer(String getContenedoresUrl) {
		try {
			URL url = new URL(getContenedoresUrl);
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			//		InetSocketAddress("proxysis", 8080));
			HttpURLConnection con = (HttpURLConnection) url.openConnection(/*proxy*/);
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				String jsonString = response.toString();
				System.out.println(jsonString);

				// Json parse
				JsonObject rootObj = new JsonParser().parse(jsonString).getAsJsonObject();
				JsonArray jsonArrayContenedores = rootObj.getAsJsonArray("value");

				for (JsonElement je : jsonArrayContenedores) {
					JsonObject jsonObjContenedor = je.getAsJsonObject();
					int thingId = jsonObjContenedor.get("@iot.id").getAsInt();
					int dsTempId = jsonObjContenedor.getAsJsonArray("Datastreams").get(1).getAsJsonObject()
							.get("@iot.id").getAsInt();
					int dsCapId = jsonObjContenedor.getAsJsonArray("Datastreams").get(0).getAsJsonObject()
							.get("@iot.id").getAsInt();
					String barrio = "";
					for (JsonElement jo: jsonObjContenedor.getAsJsonObject("properties").getAsJsonArray("barrio")){
						barrio = barrio.equals("") ? barrio+jo.getAsString() : barrio+","+jo.getAsString();
					}
					this.contenedores.put(thingId, new MiContenedor(thingId, dsCapId, dsTempId, barrio));
				}

				// Inicia threads simuladores de sensores de contenedores
				for (MiContenedor c : contenedores.values()) {
					new Thread(c).start();
				}

			} else {
				//System.out.println("Error en GET request: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadCamionesFromServer(String getCamionesUrl) {
		try {
			URL url = new URL(getCamionesUrl);
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			//		InetSocketAddress("proxysis", 8080));
			HttpURLConnection con = (HttpURLConnection) url.openConnection(/*proxy*/);
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();
			System.out.println("GET Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				String jsonString = response.toString();
				System.out.println(jsonString);

				// Json parse
				JsonObject rootObj = new JsonParser().parse(jsonString).getAsJsonObject();
				JsonArray jsonArrayCamiones = rootObj.getAsJsonArray("value");

				for (JsonElement je : jsonArrayCamiones) {
					JsonObject jsonObjCamion = je.getAsJsonObject();
					int thingId = jsonObjCamion.get("@iot.id").getAsInt();
					String barrio = jsonObjCamion.get("properties").getAsJsonObject().get("barrio").getAsString();
					this.camiones.put(thingId, new Camion(thingId, barrio));
				}

				// Inicia threads simuladores de sensores de camiones
				for (Camion c : camiones.values()) {
					new Thread(c).start();
				}

			} else {
				System.out.println("Error en GET request: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
