import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private static final int PACKET_SIZE = 10;
    private static final int TIMEOUT = 1000;

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT);
        InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

        int WINDOW_SIZE;
        System.out.print("Ingrese el tamaño de la ventana: ");
        Scanner sc = new Scanner(System.in);
        WINDOW_SIZE = sc.nextInt();

        if (WINDOW_SIZE <= 0) {
            System.out.println("El tamaño de la ventana debe ser mayor a 0.");
            return;
        }

        File file = new File("file.txt");
        if (!file.exists()) {
            System.out.println("El archivo no existe.");
            return;
        }

        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }

        int totalPackets = (int) Math.ceil((double) fileData.length / PACKET_SIZE);
        int base = 0, nextSeq = 0;
        Map<Integer, byte[]> window = new HashMap<>();
        boolean lastAckReceived = false;

        while (!lastAckReceived) {
            while (nextSeq < base + WINDOW_SIZE && nextSeq < totalPackets) {
                if (!window.containsKey(nextSeq)) {
                    int start = nextSeq * PACKET_SIZE;
                    int end = Math.min(start + PACKET_SIZE, fileData.length);
                    byte[] chunk = Arrays.copyOfRange(fileData, start, end);
                    boolean isLast = (nextSeq == totalPackets - 1);
                    byte checksum = calculateChecksum(chunk);

                    byte[] sendData = new byte[4 + chunk.length];
                    sendData[0] = (byte) (nextSeq >> 8);
                    sendData[1] = (byte) nextSeq;
                    sendData[2] = (byte) (isLast ? 1 : 0);
                    sendData[3] = checksum;
                    System.arraycopy(chunk, 0, sendData, 4, chunk.length);

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                    socket.send(sendPacket);
                    window.put(nextSeq, sendData);
                    System.out.println("Paquete enviado: " + nextSeq);
                }
                nextSeq++;
            }

            try {
                byte[] ackData = new byte[2];
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
                socket.receive(ackPacket);

                int ackNum = ((ackData[0] & 0xFF) << 8) | (ackData[1] & 0xFF);
                System.out.println("ACK recibido: " + ackNum);

                if (ackNum >= base) {
                    for (int i = base; i <= ackNum; i++) {
                        window.remove(i);
                    }
                    base = ackNum + 1;
                }

                if (base >= totalPackets) {
                    lastAckReceived = true;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout, retransmitiendo desde: " + base);
                for (int i = base; i < nextSeq; i++) {
                    if (window.containsKey(i)) {
                        DatagramPacket resendPacket = new DatagramPacket(window.get(i), window.get(i).length, serverAddress, SERVER_PORT);
                        socket.send(resendPacket);
                    }
                }
            }
        }

        socket.close();
        System.out.println("Archivo enviado exitosamente.");
    }

    private static byte calculateChecksum(byte[] data) {
        byte checksum = 0;
        for (byte b : data) checksum ^= b;
        return checksum;
    }
}
