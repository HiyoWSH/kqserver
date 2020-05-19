package kq.server.mapper;

import kq.server.bean.ImageId;

public interface ImageIdMapper {
    ImageId getImageIdByPass(String path);
    void insertImageId(ImageId imageId);
}
