import java.io.BufferedReader;
import java.io.IOException;
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
	private static final String SERVER_IP = "localhost";
	private static final String SERVER_PORT = "8080";
	private static final String GET_CONTENEDORES_URL = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Contenedor')&$expand=Datastreams($select=id)&$top=500";
	private static final String GET_CAMIONES_URL = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Camion')&$expand=Locations($select=id)&$top=500";

	private HashMap<Integer, MiContenedor> contenedores;
	private HashMap<Integer, Camion> camiones;

	private ControladorBL() {
		this.contenedores = new HashMap<Integer, MiContenedor>();
		this.camiones = new HashMap<Integer, Camion>();

		loadContenedoresFromServer();
		// loadCamionesFromServer();
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

	public void setEstado(Integer id, String estado) {
		try {
			contenedores.get(id).setEstado(estado);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadContenedoresFromServer() {
		try {
			URL url = new URL(GET_CONTENEDORES_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
				JsonArray jsonArrayContenedores = rootObj.getAsJsonArray("value");

				for (JsonElement je : jsonArrayContenedores) {
					JsonObject jsonObjContenedor = je.getAsJsonObject();
					int thingId = jsonObjContenedor.get("@iot.id").getAsInt();
					int dsTempId = jsonObjContenedor.getAsJsonArray("Datastreams").get(0).getAsJsonObject()
							.get("@iot.id").getAsInt();
					int dsCapId = jsonObjContenedor.getAsJsonArray("Datastreams").get(1).getAsJsonObject()
							.get("@iot.id").getAsInt();
					this.contenedores.put(thingId, new MiContenedor(thingId, dsCapId, dsTempId));
				}

				// Inicia threads simuladores de sensores de contenedores
				for (MiContenedor c : contenedores.values()) {
					new Thread(c).start();
				}

			} else {
				System.out.println("Error en GET request: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadCamionesFromServer() throws IOException {
		try {
			URL url = new URL(GET_CAMIONES_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
				JsonArray jsonArrayContenedores = rootObj.getAsJsonArray("value");

				for (JsonElement je : jsonArrayContenedores) {
					JsonObject jsonObjContenedor = je.getAsJsonObject();
					int thingId = jsonObjContenedor.get("@iot.id").getAsInt();
					this.camiones.put(thingId, new Camion(thingId));
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
