package kq.server.service.impl;

import kq.server.config.Configuation;
import kq.server.service.ImageService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static kq.server.util.MessageUtil.logger;
import static kq.server.util.RandomUtil.getNextInt;

@Service
public class ImageServiceImpl implements ImageService {

    private static List imgIndex;

    @Override
    public void initImageCache() {
        long start = System.currentTimeMillis();
        File f = new File(Configuation.getSetudir());
        imgIndex = getImages(f);
        logger.info("initStoryCache end " + (System.currentTimeMillis()-start) + "ms");
    }

    @Override
    public String getImage() {
        List nbList = imgIndex;
        String targetImage = "";
        while (true && nbList.size() > 0) {
            int r = getNextInt(nbList.size());
            Object o = nbList.get(r);
            if (o instanceof String) {
                targetImage = (String) o;
            } else if (o instanceof List) {
                nbList = (List) o;
                continue;
            } else {

            }
            break;
        }
        return targetImage;
    }

    private List getImages(File fileDir){
        List list = new ArrayList();
        for(File cfile:fileDir.listFiles()){
            if(cfile.isDirectory()){
                list.addAll(getImages(cfile));
            } else if (cfile.getName().endsWith("jpg") || cfile.getName().endsWith("png")
                    || cfile.getName().endsWith("jfif") || cfile.getName().endsWith("webp")
                    || cfile.getName().endsWith("jpg_large") || cfile.getName().endsWith("jpg-large")
                    || cfile.getName().endsWith("JPG") || cfile.getName().endsWith("png")){
                list.add(cfile.getPath());
            }
        }
        return list;
    }
}
