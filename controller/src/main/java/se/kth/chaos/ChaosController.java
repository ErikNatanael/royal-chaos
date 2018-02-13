package se.kth.chaos;

import com.opencsv.CSVReader;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ChaosController {
    private final String memcachedHost = "localhost";
    private final int memcachedPort = 11211;

    public void updateMode(String memcachedKey, int lifetime, String value) {
        try {
            MemcachedClient client = new XMemcachedClient(memcachedHost, memcachedPort);
            client.set(memcachedKey, lifetime, value);
            client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MemcachedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void updateMode(Map<String, String> memcachedKV, final int lifetime) {
        try {
            MemcachedClient client = new XMemcachedClient(memcachedHost, memcachedPort);
            memcachedKV.forEach((key, value) -> {
                try {
                    client.set(key, lifetime, value);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (MemcachedException e) {
                    e.printStackTrace();
                }
            });
            client.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateModesByFile(String filepath) {
        CSVReader reader = null;
        List<String[]> tryCatchInfo = null;

        try {
            reader = new CSVReader(new FileReader(filepath));
            tryCatchInfo = reader.readAll();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> kv = new HashMap<String, String>();
        for (int i = 1; i < tryCatchInfo.size(); i++) {
            String[] line = tryCatchInfo.get(i);
            kv.put(String.format("%s,%s,%s", line[0], line[1], line[2]), line[4]);
        }
        updateMode(kv, 0);

        System.out.println("INFO ChaosController - Succeeded in updating modes!");
    }

    public static void main(String args[]) {
        ChaosController controller = new ChaosController();
        String filepath = "C:\\development\\ttorrent\\chaosMonkey.csv";
        Long lastModified = 0L;
        while (true) {
            File file = new File(filepath);
            if (file.exists() && file.lastModified() > lastModified) {
                controller.updateModesByFile(filepath);
                lastModified = file.lastModified();
            }

            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}