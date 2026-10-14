package com.example.tea.controller.user;


import com.example.tea.entity.pojo.Result;
import com.example.tea.mapper.CommunityMapper;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.UserMapper;
import com.example.tea.utils.AliOssUtil;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
@RestController
@RequestMapping("/api/user")
public class UploadController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CommunityMapper communityMapper;

    /**
     * 头像上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file){
        try {
            Thread.sleep(500);
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String name = UUID.randomUUID() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), name);
            userMapper.setAvatar(filePath, ThreadLocalUserIdUtil.getCurrentId());
            return Result.success(filePath);
        } catch (Exception e) {
            return Result.error("文件上传失败");
        }
    }

    /**
     * 更新帖子图片。
     * <p>
     * 安全修复：校验 postId 非空且必须归属当前登录用户，越权返回错误而非静默修改。
     */
    @PostMapping("/uploadPostImage")
    public Result uploadPostImage(MultipartFile file, Long postId){
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        if (postId == null) {
            return Result.error("关联postId不能为空");
        }
        // 越权前置校验：先确认帖子归属，再上传 OSS，避免为越权请求白耗存储
        Long currentUserId = ThreadLocalUserIdUtil.getCurrentId();
        if (currentUserId == null) {
            return Result.error("未登录，请先登录");
        }
        if (communityMapper.countOwnedPost(postId, currentUserId) == 0) {
            return Result.error("无权修改该帖子或帖子不存在");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件名非法");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String name = UUID.randomUUID() + extension;
            String filePath = aliOssUtil.upload(file.getBytes(), name);
            // 仍带归属条件更新：兜底防并发/校验与写入之间的状态变化
            int affected = communityMapper.addImage(filePath, postId, currentUserId);
            if (affected == 0) {
                return Result.error("无权修改该帖子或帖子不存在");
            }
            return Result.success(filePath);
        } catch (Exception e) {
            return Result.error("文件上传失败");
        }
    }

}
