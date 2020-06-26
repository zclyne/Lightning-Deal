package com.yifan.lightning.deal.task;

import com.yifan.lightning.deal.dao.ItemStockDOMapper;
import com.yifan.lightning.deal.dao.StockLogDOMapper;
import com.yifan.lightning.deal.dataobject.StockLogDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.yifan.lightning.deal.constant.DatabaseConst;

import java.util.List;

@Component
public class DatabaseRollbackTask {

    @Autowired
    StockLogDOMapper stockLogDOMapper;

    @Autowired
    ItemStockDOMapper itemStockDOMapper;

    // 当前方法执行完毕10000ms后，再次调用该方法
    // 效果即每10秒检查一次stock_log表中是否存在需要回滚的数据，如果存在，则对数据库进行回滚
    @Scheduled(fixedDelay = 10000)
    public void checkStockLogAndRollback() {
        List<StockLogDO> stockLogDOList = stockLogDOMapper.selectByStatus(DatabaseConst.STOCK_LOG_STATUS_ROLLBACK);
        if (stockLogDOList != null && !stockLogDOList.isEmpty()) {
            for (StockLogDO stockLogDO : stockLogDOList) {
                itemStockDOMapper.increaseStock(stockLogDO.getItemId(), stockLogDO.getAmount());
            }
        }
    }

}
