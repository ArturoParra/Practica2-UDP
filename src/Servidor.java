import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Servidor {
    public static void main(String[] args) throws SocketException {

        final int PORT = 5000;
        byte[] buffer = new byte[1024];

        try(DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto " + PORT);

            while(true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                System.out.println("Recibiendo información del cliente");
                String mensaje = new String(packet.getData());
                System.out.println("Mensaje recibido: " + mensaje);

                int puertoCliente = packet.getPort();
                InetAddress ipCliente = packet.getAddress();

                mensaje = "Hola mundo desde el servidor";
                buffer = mensaje.getBytes();

                DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length, ipCliente, puertoCliente);

                System.out.println("Enviando respuesta al cliente con puerto " + puertoCliente + " e IP " + ipCliente);
                socket.send(respuesta);
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
