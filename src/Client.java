import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    public Client() {
        startClient();
    }

    Socket socket;
    private void startClient() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    socket = new Socket();
                    socket.connect(new InetSocketAddress("localhost", 5001));
                    System.out.println("[연결완료] " + socket.getRemoteSocketAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                    if(!socket.isClosed()){
                        stopClient();
                    }
                    return;
                }
                receive();
            }
        };
        // 생성한 스레드 시작
        thread.start();
    }

    void receive() {
        // 계속해서 데이터를 받음
        System.out.println("데이터 받음");
        while(true){
            try{
                System.out.println("데이터 받음 func");
                byte[] byteArr = new byte[100];
                InputStream inputStream = socket.getInputStream();
                // 받은 데이터를 byteArr에 저장
                inputStream.read(byteArr);

                System.out.println("[받은 메시지] " + new String(byteArr));
            } catch (IOException e) {
                e.printStackTrace();
                stopClient();
                break;
            }
        }
    }

    void send(String data){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    System.out.println("[보낸 메시지] " + data);
                    byte[] byteArr = data.getBytes();
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(byteArr);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    stopClient();
                }
            }
        };
        thread.start();
    }

    // 실제로는 수행안함
    void stopClient() {
        try{
            if(socket != null && !socket.isClosed()){
                socket.close();
                System.out.println("[통신종료]");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
