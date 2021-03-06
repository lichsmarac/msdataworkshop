package io.helidon.data.examples;

public class OrderServiceCPUStress {
    boolean isStressOn = false;

    public void start() {
        isStressOn = true;
        for (int thread = 0; thread < 4; thread++) {
            new CPUStressThread().start();
        }
    }

    public void stop() {
        isStressOn = false;
    }

    private class CPUStressThread extends Thread {
        public void run() {
            try {
                System.out.println("CPUStressThread.run isStressOn:" + isStressOn + " thread:" + Thread.currentThread());
                while (isStressOn) {
                    if (System.currentTimeMillis() % 100 == 0) {
                        Thread.sleep((long) Math.floor((.2) * 100));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
