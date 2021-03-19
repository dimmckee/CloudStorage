package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * Обработчик входящих клиентов
 */

public class ClientHandler implements Runnable {
    private final Socket socket;

    private final static int BUFFER_SIZE = 256;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())){
            while (true) {
                String command = in.readUTF();
                System.out.println("Command - " + command);
                if ("upload".equals(command)) {
                    try {
                        File file = new File("server" + File.separator + in.readUTF());
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        long size = in.readLong();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        fos.close();
                        out.writeUTF("DONE");
                    } catch (Exception e) {
                        out.writeUTF("ERROR");
                    }
                } else if ("download".equals(command)) {
                    try {
                        File file = new File("server" + File.separator + in.readUTF());
                        if (file.exists()) {
                            out.writeUTF(file.getName());
                            long length = file.length();
                            // отправляем длину файла, который будем передавать
                            out.writeLong(length);
                            FileInputStream fis = new FileInputStream(file);
                            int read = 0;
                            byte[] buffer = new byte[BUFFER_SIZE];
                            while ((read = fis.read(buffer)) != -1) {
                                // частями отправляем файл
                                out.write(buffer, 0, read);
                            }
                            out.flush();
                            String status = in.readUTF();
                            System.out.println("File Status = " + status);
                        }
                    } catch (Exception e) {
                        out.writeUTF("Error");
                    }
                } else if ("remove".equals(command)) {
                    try {
                        File file = new File("server" + File.separator + in.readUTF());
                        if (Files.deleteIfExists(file.toPath())){
                            out.writeUTF("Delete success");
                        }
                    } catch (IOException e) {
                        out.writeUTF("Error");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
