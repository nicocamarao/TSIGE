import java.util.concurrent.ThreadLocalRandom;

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
					} else {
						contenedor[2] = String.valueOf(nuevo);
					}
				} else if (contenedor[4].equals("CAMION")) {
					contenedor[2] = "0";
					contenedor[4] = "DISPONIBLE";
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
}