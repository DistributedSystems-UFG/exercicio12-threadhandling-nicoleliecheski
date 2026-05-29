public class SimpleThreads {

    // Display a message, preceded by the name of the current thread
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop
        implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    // Print a message
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    private static class ContadorPrimos
        implements Runnable {
        public void run() {
            final long upBound = 200_000_000L;
            long primos = 0;
            for (long n = 2; n <= upBound; n++) {
                if ((n & 0xFFFF) == 0 && Thread.currentThread().isInterrupted()) {
                    threadMessage("interrompido em n=" + n + " (primos: " + primos + ")");
                    return;
                }
                boolean ehPrimo = true;
                for (long d = 2; d * d <= n; d++) {
                    if (n % d == 0) {
                        ehPrimo = false;
                        break;
                    }
                }
                if (ehPrimo) {
                    primos++;
                }
            }
            threadMessage("numeros primos ate " + upBound + " = " + primos);
        }
    }

    public static void main(String args[])
        throws InterruptedException {

        // Delay, in milliseconds before we interrupt MessageLoop thread (default one hour)
        long patience = 1000 * 60 * 60;

        // If command line argument present, gives patience in seconds
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t = new Thread(new MessageLoop());

	// Put the MessageLoop thread to run
        t.start();

        threadMessage("iniciando thread ContadorPrimos");
        Thread cpu = new Thread(new ContadorPrimos(), "ContadorPrimos");
        cpu.start();

        threadMessage("Waiting for MessageLoop thread to finish");

        // loop until MessageLoop thread exits
        while (t.isAlive()) {
            threadMessage("Still waiting...");
            // Wait maximum of 1 second for MessageLoop thread to finish
            t.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && t.isAlive()) {
                threadMessage("Tired of waiting!");
		// Force the interruption of the MainLoop thread
                t.interrupt();
                // ...and wait for it to finish -- shouldn't be long now
                t.join();
            }
        }

        threadMessage("esperando a thread ContadorPrimos terminar");
        while (cpu.isAlive()) {
            threadMessage("esperando..");
            cpu.join(1000);
            if (((System.currentTimeMillis() - startTime) > patience) && cpu.isAlive()) {
                threadMessage("ContadorPrimos demorou demais, interrompendo");
                cpu.interrupt();
                cpu.join();
            }
        }

        threadMessage("Finally!");
    }
}
