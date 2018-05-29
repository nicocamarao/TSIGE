import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONObject;

public class MiContenedor implements Runnable {

	public String[] contenedor;

	public MiContenedor(String[] strings) {
		contenedor = strings;
	}

	public void run() {

		try {
			while (1 == 1) {
				
				Thread.sleep(400);
				// capacidad
				double random = ThreadLocalRandom.current().nextDouble(0, 1);
				if (contenedor[4].equals("DISPONIBLE")) {
					double nuevo = Double.valueOf(contenedor[2]) + random;
					if (nuevo >= 100) {
						contenedor[4] = "LLENO";
						contenedor[2]= "100";
						updateCapacidadEnGOST(Double.valueOf(contenedor[2]));
					} else {
						contenedor[2] = String.valueOf(nuevo);
						updateCapacidadEnGOST(Double.valueOf(contenedor[2]));
					}
				} else if (contenedor[4].equals("CAMION")) {
					contenedor[2] = "0";
					contenedor[4] = "DISPONIBLE";
					updateCapacidadEnGOST(Double.valueOf(contenedor[2]));
				}
				double temperatura = ThreadLocalRandom.current().nextDouble(21, 34);
				if(temperatura > 33.900)
				{
					//Alerta();
				}

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		

	}
	
	public static void updateCapacidadEnGOST(Double capacidad)
	{
//		String u = "http://" + gost + ":9080/v1.0/Observations(" + id + ")/";
//		URL url = new URL(u);
//		JSONObject body = new JSONObject();
//
//		body.put("name", "Camión " + id);
//		body.put("description", "Conteenedor " + id);
//		body.put("encodingType", "application/vnd.geo+json");
//		body.put("location", (JSONObject) ubicacion.get("location"));
	}
}