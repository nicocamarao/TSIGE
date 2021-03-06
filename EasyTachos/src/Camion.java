

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Camion implements Runnable{

	int threshold = 40; // por defecto
	JSONArray caminoaseguir = new JSONArray();
	String gost = "192.168.1.32";
	long id = 0;
	LinkedList<Long> historico = new LinkedList<Long>();//array de historicos con tope, topehist
	int topehist = 20; 
	ControladorBL cbl = ControladorBL.getInstance();
	String barrio = "";

	public Camion(int id, String barrio)
	{
		this.id=id;	
		this.barrio = barrio;
	}	
	
	@Override
	public void run() {
		try { 
			
			
			
			//CAMBIAR POR GET DEL THING
			JSONParser json = new JSONParser();
			JSONObject jobj = (JSONObject) json.parse("{" + "\"@iot.id\": 338, \"location\":{\"coordinates\": [-56.12416, -34.898242]}}");
			while (true) {
				Thread.sleep(400);
				JSONObject resultado = buscarSiguiente(jobj);
				JSONArray camino = obtenerCamino(jobj, resultado);

				for (int i = 1, largo = camino.size(); largo > i; i++) {					
					long tiempo = Long.parseLong(String.valueOf(((JSONObject) camino.get(i)).get("tiempo")));
					try {
						Thread.sleep(tiempo
								* 100/*
										 * MULTIPLICAR POR 1000 PARA PASAR A
										 * MILISEGUNDOS; PARA PROBAR SE DEJA EN
										 * LO QUE VENGA
										 */);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					updateGOST((JSONObject) camino.get(i));					

				}
				cbl.setEstado((Long)resultado.get("@iot.id"), "CAMION"); // pase por cotainer, setea el container en 0
				jobj = (JSONObject) camino.get(camino.size() - 1);
				Thread.sleep(10000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject buscarSiguiente(
			JSONObject jsontacho/* JSON con los datos del GOST del container*/) throws ParseException, IOException, org.json.simple.parser.ParseException, InterruptedException {

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
		String coef = "001";
		int largo = 0;
		boolean enhist = false;
		JSONArray arraycandidatos = new JSONArray ();
		JSONObject siguiente = null;
		
		while (largo == 0){
			//Thread.sleep(20000);
			URL url = new URL("http://" + gost + ":8080/v1.0/Locations?$filter=startswith(description,%27Contenedor%27)%20and%20geo.distance(location,%20geography%27POINT(" + ((JSONArray) ((JSONObject) jsontacho.get("location")).get("coordinates")).get(0) + "%20" + ((JSONArray) ((JSONObject) jsontacho.get("location")).get("coordinates")).get(1) + ")%27)%20lt%200."+coef);
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			//InetSocketAddress("proxysis", 8080));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(/*proxy*/);
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
			conn.disconnect(); 	
			
			JSONObject objeto = (JSONObject) json.parse(jstring);
			JSONArray resp = (JSONArray) objeto.get("value");
			
			if (resp != null) {
				largo = resp.size();			
				enhist = false;
				for (int i = 0; i < largo; i++) {
					for (int j = 0, l = historico.size(); j < l && !enhist; j++)
						enhist = historico.get(j) == ((JSONObject) resp.get(i)).get("@iot.id");
					if (((String)((JSONObject)resp.get(i)).get("name")).toUpperCase().contains("CONTENEDOR") && !enhist && 
							((Long)((JSONObject) resp.get(i)).get("@iot.id")-(Long)((JSONObject) jsontacho).get("@iot.id") != 0)) {
						//TODO POR AHORA QUEDA A LO CHACAL, HABRIA QUE HACER UN POST AL GOST PARA TRAER LA CAPACIDAD, PERO MEMORIA COMPARTIDA PAPA
										
						if (barrio.contains(cbl.getBarrio((Long) ((JSONObject) resp.get(i)).get("@iot.id"))) && cbl.getCapacidad((Long) ((JSONObject) resp.get(i)).get("@iot.id")) > threshold)	{
							arraycandidatos.add((JSONObject) resp.get(i)); 
						}
					}				
				}
				largo = arraycandidatos.size();
			} else {
				largo = 0;
			}
			coef = String.valueOf((Integer.parseInt(coef)*2));
			coef = coef.length() == 3 ? coef : (coef.length() == 2 ? "0"+coef : "00"+coef);
		}
		for (int i = 0, largocandidatos = arraycandidatos.size(); i < largocandidatos; i++) {
			// primero busco el tacho siguiente, más cercano al tacho actual				
			
			if (((String)((JSONObject)arraycandidatos.get(i)).get("name")).contains("contenedor") && !enhist && ((JSONObject) arraycandidatos.get(i)).get("@iot.id") != ((JSONObject) jsontacho).get("@iot.id")) {

				int ndist = distancia((JSONObject) arraycandidatos.get(i), jsontacho); // se
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
					siguiente = (JSONObject) arraycandidatos.get(i);
				}
			}
		}
		if (siguiente == null)
			siguiente = (JSONObject) arraycandidatos.get(0);
		historico.addFirst((Long) siguiente.get("@iot.id"));
		if (historico.size() > topehist)
			historico.removeLast();
		return siguiente;

		// var ruta = this.ruta();/*llamar a la api, parsear la respuesta etc*/;

	};

	public int distancia(JSONObject tacho1, JSONObject tacho2) throws IOException, ParseException, org.json.simple.parser.ParseException {

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

	@SuppressWarnings("unchecked")
	public JSONArray obtenerCamino(JSONObject origen, JSONObject destino) throws IOException, ParseException, org.json.simple.parser.ParseException {

		JSONArray o = (JSONArray) ((JSONObject) origen.get("location")).get("coordinates");
		JSONArray d = (JSONArray) ((JSONObject) destino.get("location")).get("coordinates");
		
		long id = (long) destino.get("@iot.id");

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
				nodo.put("@iot.id", id);
				JSONArray coord = new JSONArray();
				JSONObject loc = new JSONObject();
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lng"));
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("start_location")).get("lat"));
				loc.put("coordinates", coord);
				loc.put("type", "Point");
				nodo.put("location", loc);
				resultado.add(nodo);
				
				nodo = new JSONObject();
				nodo.put("tiempo", ((JSONObject) ((JSONObject) array.get(i)).get("duration")).get("value"));
				nodo.put("@iot.id", id);
				coord = new JSONArray();
				loc = new JSONObject();
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("end_location")).get("lng"));
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("end_location")).get("lat"));
				loc.put("coordinates", coord);
				loc.put("type", "Point");
				nodo.put("location", loc);
				resultado.add(nodo);
			} else {
				JSONObject nodo = new JSONObject();
				nodo.put("tiempo", ((JSONObject) ((JSONObject) array.get(i)).get("duration")).get("value"));
				nodo.put("@iot.id", id);
				JSONArray coord = new JSONArray();
				JSONObject loc = new JSONObject();
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("end_location")).get("lng"));
				coord.add(((JSONObject) ((JSONObject) array.get(i)).get("end_location")).get("lat"));
				loc.put("coordinates", coord);
				loc.put("type", "Point");
				nodo.put("location", loc);
				resultado.add(nodo);
			}

		}

		conn.disconnect();

		return resultado;

	};

	@SuppressWarnings({ "unchecked", "unused" })
	public void updateGOST(JSONObject ubicacion) throws IOException {

		String u = "http://" + gost + ":8080/v1.0/Things(" + id + ")/Locations";
		URL url = new URL(u);
		JSONObject body = new JSONObject();

		body.put("name", "Camion " + id);
		body.put("description", "Camion " + id);
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