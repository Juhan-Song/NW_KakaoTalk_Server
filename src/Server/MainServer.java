package Server;

import Query.ConnectDB;
import Query.Delete;
import Query.Select;
import User.User;
import Utilization.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer extends Thread {
    private final int port;
    private final HashMap<String, PrintWriter> userInfo = new HashMap<String, PrintWriter>();

    public MainServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ExecutorService MainServerPool = Executors.newFixedThreadPool(10000);

        try (ServerSocket listener = new ServerSocket(port)){
            System.out.println("Main Server Online...");
            while (true) {
                MainServerPool.execute(new MainServerProcessor(listener.accept()));
                // 클라이언트와 연결
            }
        } catch(Exception e) {
            System.out.println("Main Server Failed... " + e.getMessage());
        }
    }

    public static class MainServerProcessor implements Runnable {
        private Socket socket;
        private Scanner serverInput;
        private PrintWriter serverOutput;

        public MainServerProcessor(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println(socket.getInetAddress().getHostAddress());
                serverInput = new Scanner(socket.getInputStream());
                serverOutput = new PrintWriter(socket.getOutputStream(), true);
                // 인풋 아웃풋 설정

                while (serverInput.hasNextLine()) {
                    String request = serverInput.nextLine();

                    JSONParser parser = new JSONParser();
                    JSONObject client = (JSONObject) parser.parse(request);

                    if (!request.isEmpty()) {
                        int requestCode = Integer.parseInt(String.valueOf(client.get("code")));

                        switch (requestCode) {
                            case 3001:
                                UserProcess(client);
                                break;
                            // 유저 정보 가져오기 프로세스
                            case 3002:
                                SearchUserProcess(client);
                                break;
                            // 유저 검색 프로세스
                            case 3004:
                                UserInformationProcess(client);
                                break;
                            case 3003:
                                LogoutProcess(client);
                                break;
                            // 로그아웃 프로세스
                            default:
                                break;
                        }
                    }
                } // 클라이언트의 요청이 존재할 경우
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void UserProcess(JSONObject clientJSON) {
            System.out.println(Util.createLogString("Main", socket.getInetAddress().getHostAddress(), "User Request"));

            String user = String.valueOf(clientJSON.get("user"));
            String msg = Select.SelectUserMessage(new ConnectDB(), user);
            // 데이터베이스에서 탐색

            System.out.println(Util.createLogString("Main", socket.getInetAddress().getHostAddress(), "User Request Success"));

            HashMap<String, Object> userResponse = new HashMap<String, Object>();
            userResponse.put("user", user);
            userResponse.put("msg", msg);

            serverOutput.println(Util.createJSON(200, userResponse));
            // 클라이언트에게 결과 전송
        } // 유저 정보 가져오기 프로세스

        private void SearchUserProcess(JSONObject clientJSON) {
            System.out.println(Util.createLogString("Main", socket.getInetAddress().getHostAddress(), "Search User Request"));

            String user = String.valueOf(clientJSON.get("user"));
            String search = String.valueOf(clientJSON.get("search"));
            System.out.println(search);

            ArrayList<User> searchedUser = Select.SearchUser(new ConnectDB(), user, search);
            // 데이터베이스에서 탐색

            if (searchedUser.size() != 0) {
                HashMap<String, Object> searchResponse = new HashMap<String, Object>();
                String searched = new String(searchedUser.get(0).getNickName());
                String message = new String(searchedUser.get(0).getMessage());
                for (int i = 1; i < searchedUser.size(); i++) {
                    searched = searched + "," + searchedUser.get(i).getNickName();
                    message = message + "," + searchedUser.get(i).getMessage();
                }
                searchResponse.put("search", "true");
                searchResponse.put("searched", searched);
                searchResponse.put("message", message);
                // 클라이언트에게 전송할 유저 닉네임 저장

                serverOutput.println(Util.createJSON(200, searchResponse));
                // 클라이언트에게 결과 전송
            } // 검색된 유저가 있는 경우
            else {
                HashMap<String, Object> searchResponse = new HashMap<String, Object>();
                searchResponse.put("search", "false");
                searchResponse.put("searched", searchedUser);

                serverOutput.println(Util.createJSON(200, searchResponse));
                // 클라이언트에게 결과 전송
            } // 검색된 유저가 없는 경우
        } // 유저 검색 프로세스

        private void UserInformationProcess(JSONObject clientJSON) {
            System.out.println(Util.createLogString("Main", socket.getInetAddress().getHostAddress(), "UserInformation Request"));

            String nickName = String.valueOf(clientJSON.get("user"));
            System.out.println(nickName);

            HashMap<String, Object> userInfo = Select.SelectUserInformation(new ConnectDB(), nickName);

            serverOutput.println(Util.createJSON(200, userInfo));
        }

        private void LogoutProcess(JSONObject clientJSON) {
            System.out.println(Util.createLogString("Main", socket.getInetAddress().getHostAddress(), "Logout Request"));

            String user = String.valueOf(clientJSON.get("logout"));
            System.out.println(user);

            boolean isSuccess = Delete.Logout(new ConnectDB(), user);
            // 데이터베이스에서 삭제 및 업데이트

            if (isSuccess == true) {
                serverOutput.println(Util.createSingleJSON(200, "logout", "true"));
            } // 로그아웃에 성공한 경우
            else {
                serverOutput.println((Util.createSingleJSON(200, "logout", "false")));
            } // 로그아웃에 실패한 경우
        } // 로그아웃 프로세스
    }
}
