import java.util.Scanner;

public class ClientTest2 {
    public static void main(String[] args) {
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
