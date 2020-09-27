import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static ExecutorService executorService;
    static ServerSocket serverSocket;
    static List<ClientManager> connections = new Vector<ClientManager>();

    public void startServer(){

        executorService = Executors.newFixedThreadPool(
                // cpu 코어 수 만큼 스레드 생성
                Runtime.getRuntime().availableProcessors()
        );
        System.out.println("start!!");
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 5001));
        } catch (IOException e) {
            if(!serverSocket.isClosed()){

            }
            e.printStackTrace();
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("[연결 완료] " + socket.getRemoteSocketAddress() + " / " + Thread.currentThread().getName());


                        ClientManager client = new ClientManager(socket);
                        connections.add(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executorService.submit(runnable);
    }

    // 실제로 수행은 안됨
    public void stopServer() {
        try{
            Iterator<ClientManager> iterator = connections.iterator();
            while(iterator.hasNext()){
                ClientManager client = iterator.next();
                client.socket.close();
                iterator.remove();;
            }
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
            if(executorService != null && !executorService.isShutdown()){
                executorService.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientManager{
        Socket socket;

        ClientManager(Socket socket){
            this.socket = socket;
            receive();
        }
        void receive() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try{
                        while(true){
                            byte[] byteArr = new byte[100];
                            InputStream inputStream = socket.getInputStream();

                            int readByteCount = inputStream.read(byteArr);

                            if(readByteCount == -1){
                                throw new IOException();
                            }
                            String data = new String(byteArr);

                            System.out.println("[요청처리] " + socket.getRemoteSocketAddress() + " / " + Thread.currentThread().getName());
                            System.out.println("message : " + new String(byteArr));

                            // 연결되어있는 클라이언트들에게 전송
                            for(ClientManager client : connections){
                                if(client.socket.getRemoteSocketAddress().equals(socket.getRemoteSocketAddress())){
                                    continue;
                                }
                                // 연결되어있는 클라이언트들끼리에게만 보내기 때문에 send 사용
                                client.send(data);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 에러 발생한 클라이언트 삭제
                        connections.remove(ClientManager.this);
                        System.out.println("[통신오류로 인한 클라이언트 삭제] " + socket.getRemoteSocketAddress() + " / " + Thread.currentThread().getName() );
                    }
                }
            };
            executorService.submit(runnable);
        }

        void send(String data){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("받은 데이터 전송");
                        byte[] byteArr = data.getBytes();
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(byteArr);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            System.out.println("[클라이언트 통신 오류] " + socket.getRemoteSocketAddress() + " / " + Thread.currentThread().getName());
                            connections.remove(ClientManager.this);
                            socket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            };
            executorService.submit(runnable);
        }
    }
}
