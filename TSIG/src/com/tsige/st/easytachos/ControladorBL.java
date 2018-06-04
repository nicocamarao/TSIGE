package com.tsige.st.easytachos;
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
	private static final String SERVER_IP = "192.168.1.34";
	private static final String SERVER_PORT = "8080";
	private static final String GET_CONTENEDORES_URL_1 = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Contenedor')&$expand=Datastreams($select=id)&$select=id,Datastreams&$top=200&$skip=0";
	private static final String GET_CONTENEDORES_URL_2 = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,'Contenedor')&$expand=Datastreams($select=id)&$select=id,Datastreams&$top=200&$skip=200";
	private static final String GET_CAMIONES_URL = "http://" + SERVER_IP + ":" + SERVER_PORT
			+ "/v1.0/Things?$filter=startswith(name,%27Camion%27)&$select=id&$top=500";

	private HashMap<Long, MiContenedor> contenedores;
	private HashMap<Long, Camion> camiones;

	private ControladorBL() {
		this.contenedores = new HashMap<Long, MiContenedor>();
		this.camiones = new HashMap<Long, Camion>();

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

	public HashMap<Long, MiContenedor> getContenedores() {
		return this.contenedores;
	}

	public HashMap<Long, Camion> getCamiones() {
		return this.camiones;
	}

	public MiContenedor getContenedor(Long id) {
		try {
			return contenedores.get(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Camion getCamion(Long id) {
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

	private void loadContenedoresFromServer(String getContenedoresUrl) {
		try {
			URL url = new URL(getContenedoresUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// Parse del response json
				String jsonString = response.toString();
				JsonObject rootObj = new JsonParser().parse(jsonString).getAsJsonObject();
				JsonArray jsonArrayContenedores = rootObj.getAsJsonArray("value");

				// Se agregan contenedores al diccionario global
				for (JsonElement je : jsonArrayContenedores) {
					JsonObject jsonObjContenedor = je.getAsJsonObject();
					int thingId = jsonObjContenedor.get("@iot.id").getAsInt();
					int dsTempId = jsonObjContenedor.getAsJsonArray("Datastreams").get(1).getAsJsonObject()
							.get("@iot.id").getAsInt();
					int dsCapId = jsonObjContenedor.getAsJsonArray("Datastreams").get(0).getAsJsonObject()
							.get("@iot.id").getAsInt();
					this.contenedores.put(new Long(thingId), new MiContenedor(thingId, dsCapId, dsTempId));
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

	// Hace el GET de todos los camiones con sus ubicaciones al servidor GOST, guarda los camiones en
	// un diccionario global 'camiones' e instancia los threads simuladores de camiones 'Camion'.
	private void loadCamionesFromServer(String getCamionesUrl) {
		try {
			URL url = new URL(getCamionesUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// Parse del response json
				String jsonString = response.toString();
				JsonObject rootObj = new JsonParser().parse(jsonString).getAsJsonObject();
				JsonArray jsonArrayCamiones = rootObj.getAsJsonArray("value");

				// Se agregan camiones al diccionario global
				for (JsonElement je : jsonArrayCamiones) {
					JsonObject jsonObjCamion = je.getAsJsonObject();
					int thingId = jsonObjCamion.get("@iot.id").getAsInt();
					this.camiones.put(new Long(thingId), new Camion(thingId));
				}

				// Inicia threads simuladores de movimiento de camiones
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
