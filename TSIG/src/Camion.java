
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Camion implements Runnable{

	static int threshold = 40; // por defecto
	static JSONArray caminoaseguir = new JSONArray();
	static String gost = "18.231.190.192";
	static int id = 0;

	public Camion(int id)
	{
		this.id=331;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try { 
			
			
			JSONParser json = new JSONParser();
			JSONObject jobj = (JSONObject) json.parse("{" + "\"@iot.id\": 340, \"location\":{\"coordinates\": [-56.2594684914509, -34.8433762543554]}}");
			while (true) {
				Thread.sleep(400);
				JSONObject resultado = buscarSiguiente(jobj);
				JSONArray camino = obtenerCamino(jobj, resultado);

				for (int i = 1, largo = camino.size(); largo > i; i++) {

					long tiempo = Long.parseLong(String.valueOf(((JSONObject) camino.get(i)).get("tiempo")));
					try {
						Thread.sleep(tiempo
								* 1000/*
										 * MULTIPLICAR POR 1000 PARA PASAR A
										 * MILISEGUNDOS; PARA PROBAR SE DEJA EN
										 * LO QUE VENGA
										 */);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updateGOST((JSONObject) camino.get(i));

				}
				

				jobj = (JSONObject) camino.get(camino.size() - 1);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static JSONObject buscarSiguiente(
			JSONObject jsontacho/* JSON con los datos del GOST del 1er */) throws ParseException, IOException, org.json.simple.parser.ParseException {

		JSONParser json = new JSONParser();
		/*
		 * String response =
		 * "[{\"@iot.id\": 2, \"capacidad\": 40, \"barrio\":\"Aguada\", \"loc\":{\"lat\":-56.155623799997153, \"long\":-34.821845099999571}},"
		 * +
		 * "{\"@iot.id\": 3, \"capacidad\": 40, \"barrio\":\"Aguada\", \"loc\":{\"lat\":-56.250901199999987, \"long\":-34.821845099999956}},"
		 * +
		 * "{\"@iot.id\": 4, \"capacidad\": 40, \"barrio\":\"Aguada\", \"loc\":{\"lat\":-56.155623799999959, \"long\":-34.821845099999996}},"
		 * +
		 * "{\"@iot.id\": 5, \"capacidad\": 40, \"barrio\":\"Aguada\", \"loc\":{\"lat\":-56.155623799999969, \"long\":-34.821845099999978}},"
		 * +
		 * "{\"@iot.id\": 6, \"capacidad\": 40, \"barrio\":\"Aguada\", \"loc\":{\"lat\":-56.155623799999978, \"long\":-34.821845099999973}}]"
		 * ;
		 */
		int dist = -1;
		URL url = new URL("http://" + gost + ":9080/v1.0/Locations?$filter=geo.distance(location,%20geography%27POINT(" + ((JSONArray) ((JSONObject) jsontacho.get("location")).get("coordinates")).get(0) + "%20" + ((JSONArray) ((JSONObject) jsontacho.get("location")).get("coordinates")).get(1) + ")%27)%20lt%200.005");
		// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
		// InetSocketAddress("proxysis", 8080));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/* proxy */);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output;
		String jstring = "";
		while ((output = br.readLine()) != null) {
			jstring += output;
		}
		conn.disconnect(); // ESTE ES EL CODIGO REAL, DESCOMENTAR CUANDO ESTE EL
							// GOST

		JSONObject siguiente = null;
		JSONObject objeto = (JSONObject) json.parse(jstring);
		JSONArray resp = (JSONArray) objeto.get("value");
		for (int i = 0, largo = resp.size(); i < largo; i++) {
			// primero busco el tacho siguiente, más cercano al tacho actual

			if (((JSONObject) resp.get(i)).get("@iot.id") != ((JSONObject) jsontacho).get("@iot.id")) {

				int ndist = distancia((JSONObject) resp.get(i), jsontacho); // se
																			// puede
																			// tambien
																			// llamar
																			// a
																			// la
																			// api
																			// con
																			// el
																			// origen
																			// y
																			// todos
																			// los
																			// destinos
				// recalculo distancia
				if ((dist == -1 || ndist < dist) && ndist != 0) {
					dist = ndist;
					siguiente = (JSONObject) resp.get(i);
				}
			}
		}
		return siguiente;

		// var ruta = this.ruta();/*llamar a la api, parsear la respuesta etc*/;

	};

	public static int distancia(JSONObject tacho1, JSONObject tacho2) throws IOException, ParseException, org.json.simple.parser.ParseException {

		JSONParser json = new JSONParser();

		JSONArray coord1 = ((JSONArray) ((JSONObject) tacho1.get("location")).get("coordinates"));
		JSONArray coord2 = ((JSONArray) ((JSONObject) tacho2.get("location")).get("coordinates"));

		String u = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + coord1.get(1) + "," + coord1.get(0) + "&destinations=" + coord2.get(1) + "," + coord2.get(0) + "&key=AIzaSyCzfvttiB7u5kfWO1in02KC5nHEluo0_Z8";
		URL url = new URL(u);
		// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
		// InetSocketAddress("proxysis", 8080));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/* proxy */);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output = "";
		String jstring = "";
		while ((output = br.readLine()) != null) {
			jstring += output;
		}

		JSONObject jsonaux = (JSONObject) json.parse(jstring);
		JSONArray jarray = (JSONArray) jsonaux.get("rows");
		JSONObject duration = (JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) jarray.get(0)).get("elements")).get(0)).get("duration");
		JSONObject distance = (JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) jarray.get(0)).get("elements")).get(0)).get("distance");

		conn.disconnect();

		int resultado = Integer.parseInt(duration.get("value").toString()) + Integer.parseInt(distance.get("value").toString());

		return resultado;

	};

	public static JSONArray obtenerCamino(JSONObject origen, JSONObject destino) throws IOException, ParseException, org.json.simple.parser.ParseException {

		JSONArray o = (JSONArray) ((JSONObject) origen.get("location")).get("coordinates");
		JSONArray d = (JSONArray) ((JSONObject) destino.get("location")).get("coordinates");

		String u = "https://maps.googleapis.com/maps/api/directions/json?units=imperial&origin=" + o.get(1) + "," + o.get(0) + "&destination=" + d.get(1) + "," + d.get(0) + "&key=AIzaSyCJLTXZgybaCSMg_aiHJlPBkVQeKnZgMTE";

		URL url = new URL(u);
		// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
		// InetSocketAddress("proxysis", 8080));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/* proxy */);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output = "";
		String jstring = "";
		while ((output = br.readLine()) != null) {
			jstring += output;
		}

		JSONParser json = new JSONParser();

		JSONObject camino = (JSONObject) json.parse(jstring);

		JSONArray array = ((JSONArray) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) camino.get("routes")).get(0)).get("legs")).get(0)).get("steps"));
		JSONArray resultado = new JSONArray();
		for (int i = 0, largo = array.size(); i < largo; i++) {
			if (i == 0) {
				JSONObject nodo = new JSONObject();
				nodo.put("tiempo", "0");
				JSONArray coord = new JSONArray();
				JSONObject loc = new JSONObject();
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lng"));
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lat"));
				loc.put("coordinates", coord);
				loc.put("type", "Point");
				nodo.put("location", loc);
				resultado.add(nodo);
			}

			JSONObject nodo = new JSONObject();
			nodo.put("tiempo", ((JSONObject) ((JSONObject) array.get(i)).get("duration")).get("value"));
			JSONArray coord = new JSONArray();
			JSONObject loc = new JSONObject();
			coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lng"));
			coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lat"));
			loc.put("coordinates", coord);
			loc.put("type", "Point");
			nodo.put("location", loc);
			resultado.add(nodo);

		}

		conn.disconnect();

		return resultado;

	};

	public static void updateGOST(JSONObject ubicacion) throws IOException {

		String u = "http://" + gost + ":9080/v1.0/Things(" + id + ")/Locations";
		URL url = new URL(u);
		JSONObject body = new JSONObject();

		body.put("name", "Camión " + id);
		body.put("description", "Camión " + id);
		body.put("encodingType", "application/vnd.geo+json");
		body.put("location", (JSONObject) ubicacion.get("location"));

		/*
		 * Proxy proxy = new Proxy(Proxy.Type.HTTP, new
		 * InetSocketAddress("proxysis", 8080));
		 */
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/* proxy */);
		conn.setDoOutput(true);
		// conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");

		OutputStream wr = conn.getOutputStream();

		wr.write(body.toString().getBytes());
		wr.flush();

		if (conn.getResponseCode() != 201) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		String output = "";
		String jstring = "";
		while ((output = br.readLine()) != null) {
			jstring += output;
		}

		conn.disconnect();

	}

	

}