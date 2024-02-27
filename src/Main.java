import util.TimeToLiveHashMap;
import util.TimeToLiveEnum;

import java.io.IOException;


public class Main {

    static String[] test = {"테스트1", "테스트2", "테스트3", "테스트4", "테스트5"};


    public static void main(String[] args) throws InterruptedException, IOException {

        TimeToLiveHashMap<String, String> expireHashMap = new TimeToLiveHashMap();
        Thread putThread = new putThread(expireHashMap);

        putThread.start();

        Thread getThread = new getThread(expireHashMap);

        getThread.start();


        expireHashMap.putWithExpired("테스트", "치환메시지가 이렇게 들어가게 될 거다", TimeToLiveEnum.CREATED, 1000);

    }

    public static class getThread extends Thread {

        private final TimeToLiveHashMap expireHashMap;
        public getThread(TimeToLiveHashMap expireHashMap) {
            this.expireHashMap = expireHashMap;
        }

        @Override
        public void run() {
            while (true) {

                for (String s : test) {
                    Object o = expireHashMap.get(s);
                    System.out.println("geto = " + o);
                }

                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class putThread extends Thread {

        private final TimeToLiveHashMap expireHashMap;
        public putThread(TimeToLiveHashMap expireHashMap) {
            this.expireHashMap = expireHashMap;
        }

        @Override
        public void run() {
            while (true) {

                int count = 0;
                for (String s : test) {
                    Object o = expireHashMap.putWithExpired(s, "하루하루지나면" + count++, TimeToLiveEnum.CREATED, 100);
                    System.out.println("puto = " + o);
                }

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}