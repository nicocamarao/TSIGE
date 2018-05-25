import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dalegas {

	public static void main(String[] args) throws IOException {
		String contenedoresJSON = readFileAsString("/Users/nicolasfuimarelli/Downloads/capaContenedoresBarrios.json");
		List<Object[]> contenedores = new ArrayList<Object[]>();
		String[] spliteadoProperties = contenedoresJSON.split("properties\":");
		for (int i = 2; i < spliteadoProperties.length; i++) {
			String barrio = spliteadoProperties[i].split("nombbarr\": \"")[1].split("\"")[0];
			Double coordenadaX = Double.valueOf(spliteadoProperties[i].split("\"coordinates\":")[1].split(",")[0].substring(3));
			Double coordenadaY = Double.valueOf(spliteadoProperties[i].split("coordinates\":")[1].split(", ")[1].split(" ")[0]);
			contenedores.add(new Object[]{barrio, coordenadaX, coordenadaY});
		}

	}

	public static String readFileAsString(String path) throws java.io.IOException {
		byte[] buffer = new byte[(int) new File(path).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(new File(path).toString()));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}
}
