package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final static int BUFFER_SIZE = 256;

    public Client() throws IOException {
        socket = new Socket("localhost", 1234);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        runClient();
    }

    private void runClient() {
        JFrame frame = new JFrame("Cloud Storage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
/*
        // создание списка файлов
        JList<String> list = new JList<>();
        DefaultListModel<String> myModel = new DefaultListModel<>();
        list.setModel(myModel);
*/
        JTextArea ta = new JTextArea();
        // TODO: 02.03.2021
        // list file - JList
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
/*
        frame.getContentPane().add(BorderLayout.NORTH, ta);
        frame.getContentPane().add(BorderLayout.CENTER, new JScrollPane(list));
        frame.getContentPane().add(BorderLayout.SOUTH, uploadButton);

        fillList(myModel);
*/
        frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
        frame.getContentPane().add(BorderLayout.CENTER, ta);
        frame.getContentPane().add(BorderLayout.SOUTH, uploadButton);
        frame.getContentPane().add(BorderLayout.SOUTH, downloadButton);

        frame.setVisible(true);

        uploadButton.addActionListener(a -> {
            System.out.println(sendFile(ta.getText()));
        });
        downloadButton.addActionListener(a -> {
            try {
                downloadFileList(ta.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
//    private void fillList(DefaultListModel<String> myModel) {
//
//        myModel.clear();
//        for (String filename : list) {
//            myModel.addElement(filename);
//        }
//    }

    private String downloadFileList(String fileName) throws IOException {
        try {
            File file = new File("server" + File.separator + in.readUTF());
            if (!file.exists()) {
                file.createNewFile();
            }
            long size = in.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) { // FIXME
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            out.writeUTF("DONE");
        } catch (Exception e) {
            out.writeUTF("ERROR");
        }
        return "";
    }
    private String sendFile(String filename) {
        try {
            File file = new File("client" + File.separator + filename);
            if (file.exists()) {
                out.writeUTF("upload");
                out.writeUTF(filename);
                long length = file.length();
                out.writeLong(length);
                FileInputStream fis = new FileInputStream(file);
                int read = 0;
                byte[] buffer = new byte[256];
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                String status = in.readUTF();
                System.out.println("File Status = " + status);
                return status;
            } else {
                return "File is not exists";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Something error";
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}
