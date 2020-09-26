import java.util.Scanner;

public class ReceiveTest {
    public static void main(String[] args) {
//        ReceiveClient receiveClient = new ReceiveClient();
        Scanner sc = new Scanner(System.in);
        Client client = new Client();
        while(true){
            String input = sc.nextLine();
            if(input.equals("exit")){
                client.stopClient();
                return;
            }else {
                client.send(input);
            }
        }

    }
}
