import java.io.IOException;
import java.net.*;

public class Cliente {

    public static void main(String[] args) {

        final int SERVER_PORT = 5000;
        byte[] buffer = new byte[1024];

        try(DatagramSocket socket = new DatagramSocket()){

            InetAddress SERVER_IP = InetAddress.getByName("localhost");

            String mensaje = "Hola Mundo, desde el cliente";

            buffer = mensaje.getBytes();

            DatagramPacket pregunta = new DatagramPacket(buffer, buffer.length, SERVER_IP, SERVER_PORT);

            System.out.println("Enviando mensaje al servidor");

            socket.send(pregunta);

            DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);

            System.out.println("Recibiendo respuesta del servidor");
            socket.receive(respuesta);

            mensaje = new String(respuesta.getData());

            System.out.println(mensaje);

        }catch (SocketException e){
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
