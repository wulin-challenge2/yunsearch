package com.github.gin.yunsearch.agama.process;

import com.github.gin.agama.core.ContextHolder;
import com.github.gin.agama.processer.PageProcess;
import com.github.gin.agama.site.Page;
import com.github.gin.agama.site.Request;
import com.github.gin.yunsearch.agama.Constant;
import com.github.gin.yunsearch.agama.YunRequestFactory;
import com.github.gin.yunsearch.agama.entity.YDataList;
import com.github.gin.yunsearch.model.YunUser;
import com.github.gin.yunsearch.repository.YunUserRepository;
import com.github.gin.yunsearch.service.YunUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author GinPonson
 * @since 2017/4/2
 */
@Component
public class YDataListPageProcess implements PageProcess<YDataList> {

    @Autowired
    private YunUserService yunUserService;

    @Override
    public void process(Page page, List<YDataList> yDatasList) {
        Matcher m = Constant.YUN_PATTERN.matcher(page.getUrl());
        if (m.find()) {
            long uk = Long.parseLong(m.group(1));
            int start = Integer.parseInt(m.group(2));

            //设置该用户的资源是否已获取完毕
            int dataCount = 0;
            if(yDatasList != null && !yDatasList.isEmpty()) {
                dataCount = yDatasList.get(0).getTotalCount();
            }
            if ((start + Constant.LIMIT) >= dataCount) {
                yunUserService.setPubshareCrawledComplete(uk);

                List<YunUser> yunUserList = yunUserService.findPubshareNeedCrawle();
                for(YunUser yunUser : yunUserList) {
                    Request request = YunRequestFactory.create();
                    request.setUrl(String.format(Constant.YUN_URL, yunUser.getUk(), 0));
                    ContextHolder.getContext().getScheduler().push(request);
                }
            } else {
                Request request = YunRequestFactory.create();
                request.setUrl(String.format(Constant.YUN_URL, uk, start + Constant.LIMIT));
                ContextHolder.getContext().getScheduler().push(request);
            }

        }
    }
}
