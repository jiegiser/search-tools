package com.searchtools.service;

import com.searchtools.model.PoJieResource;
import com.searchtools.repository.PoJieResourceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final PoJieResourceRepository poJieResourceRepository;

    @Value("${notification.email.to:jiegiser@163.com}")
    private String toEmail;

    @Value("${notification.email.from:${MAIL_USERNAME:jiegiser@163.com}}")
    private String fromEmail;

    @Value("${spring.mail.username:${MAIL_USERNAME:jiegiser@163.com}}")
    private String mailUsername;

    /**
     * 发送新资源通知邮件
     *
     * @param resources 新发现的资源列表
     * @return 是否发送成功
     */
    public boolean sendNewResourceNotification(List<PoJieResource> resources) {
        if (resources == null || resources.isEmpty()) {
            log.info("没有新资源需要通知");
            return true;
        }

        try {
            String subject = "吾爱破解资源更新 - 发现 " + resources.size() + " 个新资源";
            String htmlContent = buildHtmlContent(resources);

            sendHtmlEmail(toEmail, subject, htmlContent);

            // 更新通知状态
            List<Long> ids = resources.stream()
                    .map(PoJieResource::getId)
                    .collect(Collectors.toList());
            poJieResourceRepository.updateNotifiedStatus(ids, LocalDateTime.now());

            log.info("资源通知邮件已发送，共{}个资源", resources.size());
            return true;
        } catch (Exception e) {
            log.error("发送资源通知邮件失败", e);
            return false;
        }
    }

    /**
     * 发送HTML邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param html    HTML内容
     */
    public void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
        log.info("邮件已发送至: {}", to);
    }

    /**
     * 构建HTML邮件内容
     */
    private String buildHtmlContent(List<PoJieResource> resources) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5; padding: 20px; }");
        html.append(".container { max-width: 800px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".header p { margin: 5px 0 0; opacity: 0.9; }");
        html.append(".content { padding: 20px; }");
        html.append(".resource-card { border: 1px solid #e0e0e0; border-radius: 6px; margin: 15px 0; padding: 15px; background: #fafafa; }");
        html.append(".resource-card:hover { border-color: #667eea; }");
        html.append(".resource-title { font-size: 16px; font-weight: bold; color: #333; margin-bottom: 10px; }");
        html.append(".resource-title a { color: #667eea; text-decoration: none; }");
        html.append(".resource-info { color: #666; font-size: 14px; line-height: 1.6; }");
        html.append(".resource-link { margin-top: 10px; }");
        html.append(".resource-link a { color: #fff; background: #667eea; padding: 8px 16px; border-radius: 4px; text-decoration: none; display: inline-block; }");
        html.append(".extract-code { background: #fff3cd; color: #856404; padding: 4px 8px; border-radius: 4px; font-family: monospace; }");
        html.append(".best-answer { color: #28a745; font-weight: bold; }");
        html.append(".footer { padding: 15px 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center; color: #666; font-size: 12px; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>吾爱破解资源更新通知</h1>");
        html.append("<p>发现 ").append(resources.size()).append(" 个新的网盘资源</p>");
        html.append("</div>");
        html.append("<div class='content'>");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (PoJieResource resource : resources) {
            html.append("<div class='resource-card'>");
            html.append("<div class='resource-title'>");
            html.append("<a href='").append(resource.getThreadUrl()).append("'>").append(resource.getThreadTitle()).append("</a>");
            if (resource.getIsBestAnswer()) {
                html.append(" <span class='best-answer'>[最佳答案]</span>");
            }
            html.append("</div>");
            html.append("<div class='resource-info'>");
            html.append("<p><strong>网盘类型：</strong>").append(resource.getPanType().getDisplayName()).append("</p>");
            if (resource.getExtractCode() != null && !resource.getExtractCode().isEmpty()) {
                html.append("<p><strong>提取码：</strong><span class='extract-code'>").append(resource.getExtractCode()).append("</span></p>");
            }
            html.append("<p><strong>匹配关键词：</strong>").append(resource.getMatchedKeywords()).append("</p>");
            html.append("<p><strong>回复楼层：</strong>第").append(resource.getReplyFloor()).append("楼</p>");
            html.append("<p><strong>爬取时间：</strong>").append(resource.getCreatedAt().format(formatter)).append("</p>");
            html.append("</div>");
            html.append("<div class='resource-link'>");
            html.append("<a href='").append(resource.getResourceUrl()).append("'>访问网盘链接</a>");
            html.append("</div>");
            html.append("</div>");
        }

        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>此邮件由全网盘搜索引擎自动发送</p>");
        html.append("<p>访问 <a href='http://localhost:5174'>搜索平台</a> 查看更多资源</p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    /**
     * 获取未通知的资源
     *
     * @param limit 数量限制
     * @return 未通知的资源列表
     */
    public List<PoJieResource> getUnnotifiedResources(int limit) {
        Page<PoJieResource> page = poJieResourceRepository.findByNotifiedFalseOrderByCreatedAtDesc(
                PageRequest.of(0, limit));
        return page.getContent();
    }

    /**
     * 发送测试邮件
     *
     * @return 是否发送成功
     */
    public boolean sendTestEmail() {
        try {
            String subject = "全网盘搜索引擎 - 测试邮件";
            String html = "<html><body>"
                    + "<h2>邮件配置测试</h2>"
                    + "<p>如果您收到此邮件，说明邮件通知功能已配置成功。</p>"
                    + "<p>系统将每10分钟自动检查吾爱破解论坛的新资源，并通过此邮箱发送通知。</p>"
                    + "<p>时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>"
                    + "</body></html>";

            sendHtmlEmail(toEmail, subject, html);
            log.info("测试邮件发送成功");
            return true;
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            return false;
        }
    }
}
