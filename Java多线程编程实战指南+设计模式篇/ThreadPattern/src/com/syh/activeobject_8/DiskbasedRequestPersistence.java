package com.syh.activeobject_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-5
 * Time: 上午10:54
 * To change this template use File | Settings | File Templates.
 */
public class DiskbasedRequestPersistence implements RequestPersistence{

    // 负责缓存文件的存储管理
    private final SectionBasedDiskStorage storage = new SectionBasedDiskStorage();

    @Override
    public void store(Recipient recipient) {
        // 申请缓存文件的文件名
        String[] fileNameParts = storage.apply4Filename(recipient);
        File file = new File(fileNameParts[0]);

        ObjectOutputStream objout = null;
        try {
            objout = new ObjectOutputStream(new FileOutputStream(file));
            objout.writeObject(objout);
        }
        catch (IOException e) {
            storage.decrementSectionFileCount(fileNameParts[1]);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            if(null != objout){
                try {
                    objout.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    private class SectionBasedDiskStorage {
        private Deque<String> sectionNames = new LinkedList<String>();
        private String storageBaseDir = System.getProperty("user.dir") + "/vpn";

        private int maxFilesPerSection = 2000;
        private int maxSectionCount = 100;

        private Map<String, AtomicInteger> sectionFileCountMap = new HashMap<String, AtomicInteger>();
        private final Object sectionLock = new Object();

        public String[] apply4Filename(Recipient recipient) {
            String sectionName;
            int iFileCount;
            boolean need2RemoveSection = false;

            String[] fileName = new String[2];
            synchronized (sectionLock){
                // 获取存储子目录名
                sectionName = this.getSectionName();
                AtomicInteger fileCount = sectionFileCountMap.get(sectionName);
                iFileCount = fileCount.get();

                // 当前子目录已满
                if(iFileCount >= maxFilesPerSection){
                    if(sectionNames.size() >= maxSectionCount){
                        need2RemoveSection = true;
                    }

                    sectionName = this.makeNewSectionDir();
                    fileCount = sectionFileCountMap.get(sectionName);
                }
                iFileCount = fileCount.addAndGet(1);
            }

            fileName[0] = storageBaseDir +"/"+ sectionName +"/"+ System.currentTimeMillis() +".rp";
            fileName[1] = sectionName;

            if(need2RemoveSection){
                String oldsetSectionName = sectionNames.removeFirst();
                this.removeSection(oldsetSectionName);
            }
            return fileName;
        }

        private String getSectionName() {
            return "";
        }

        private String makeNewSectionDir() {
            return "";
        }

        private void removeSection(String oldsetSectionName) {

        }

        public void decrementSectionFileCount(String fileNamePart) {
        }
    }
}
