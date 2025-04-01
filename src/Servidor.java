import java.io.*;
import java.net.*;

public class Servidor {
    private static final int PORT = 9876;
    private static final int PACKET_SIZE = 10;
    private static final String OUTPUT_FILE = "received_file.txt";

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(PORT);
        int expectedSeq = 0;
        boolean lastPacketReceived = false;
        FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);

        while (!lastPacketReceived) {
            byte[] receiveData = new byte[PACKET_SIZE + 4];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            int seqNum = ((receiveData[0] & 0xFF) << 8) | (receiveData[1] & 0xFF);
            boolean isLast = receiveData[2] == 1;
            byte checksum = receiveData[3];
            byte[] fileData = new byte[receivePacket.getLength() - 4];
            System.arraycopy(receiveData, 4, fileData, 0, fileData.length);

            if (checksum != calculateChecksum(fileData)) {
                System.out.println("Checksum incorrecto, descartando paquete " + seqNum);
                sendAck(socket, receivePacket.getAddress(), receivePacket.getPort(), expectedSeq - 1);
                continue;
            }

            if (seqNum == expectedSeq) {
                fos.write(fileData);
                expectedSeq++;
                if (isLast) lastPacketReceived = true;
            }

            sendAck(socket, receivePacket.getAddress(), receivePacket.getPort(), expectedSeq - 1);
        }

        fos.close();
        socket.close();
        System.out.println("Archivo recibido exitosamente.");
    }

    private static void sendAck(DatagramSocket socket, InetAddress address, int port, int seqNum) throws IOException {
        byte[] ackData = new byte[]{(byte) (seqNum >> 8), (byte) seqNum};
        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, address, port);
        socket.send(ackPacket);
        System.out.println("ACK enviado: " + seqNum);
    }

    private static byte calculateChecksum(byte[] data) {
        byte checksum = 0;
        for (byte b : data) checksum ^= b;
        return checksum;
    }
}
