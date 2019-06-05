package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.mapper.*;
import com.mod.loan.model.*;
import com.mod.loan.service.UserService;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.aliyun.OSSUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl  extends BaseServiceImpl< User,Long> implements UserService{

	private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	UserMapper userMapper;
	@Autowired
	UserIdentMapper userIdentMapper;
	@Autowired
	UserAddressListMapper addressListMapper;
	@Autowired
	UserInfoMapper userInfoMapper;
	@Autowired
	UserBankMapper userBankMapper;
	@Autowired
	LoanMarketStatMapper loanMarketStatMapper;
	@Autowired
	RedisMapper redisMapper;

	@Override
	public User selectUserByPhone(String userPhone,String merchant) {
		// TODO Auto-generated method stub
		User u=new User();
		u.setUserPhone(userPhone);
		u.setMerchant(merchant);
		return userMapper.selectOne(u);
	}

	@Override
	public Long addUser(String phone,String password,String userOrigin,String merchant, String browserType) {
        //自然流量暂时记录到指定渠道上
		if (userOrigin.equalsIgnoreCase("ios")
				||userOrigin.equalsIgnoreCase("android")){
			userOrigin = String.valueOf(Constant.NATURE_ORIGIN_ID);
		}
		User user=new User();
		user.setUserPhone(phone);
		user.setUserPwd(password);
		user.setUserOrigin(userOrigin);
		user.setMerchant(merchant);
        user.setUserNick(browserType);  //存储浏览器类型,方便统计是QQ还是微信进来的
		userMapper.insertSelective(user);
		UserIdent userIdent=new UserIdent();
		userIdent.setUid(user.getId());
		userIdent.setCreateTime(new Date());
		userIdentMapper.insertSelective(userIdent);
		UserAddressList addressList=new UserAddressList();
		addressList.setUid(user.getId());
		addressList.setCreateTime(new Date());
		addressListMapper.insertSelective(addressList);
		UserInfo userInfo=new UserInfo();
		userInfo.setUid(user.getId());
		userInfo.setCreateTime(new Date());
		userInfoMapper.insertSelective(userInfo);

		return  user.getId();
	}

	@Override
	public void updateUserRealName(User user, UserIdent userIdent) {
		// TODO Auto-generated method stub
		userMapper.updateByPrimaryKeySelective(user);
		userIdentMapper.updateByPrimaryKeySelective(userIdent);
	}

	@Override
	public User selectUserByCertNo(String userCertNo, String merchant) {
		// TODO Auto-generated method stub
		User u=new User();
		u.setUserCertNo(userCertNo);
		u.setMerchant(merchant);
		return userMapper.selectOne(u);
	}

	@Override
	public void updateUserInfo(UserInfo userInfo,UserIdent userIdent) {
		// TODO Auto-generated method stub
		userInfoMapper.updateByPrimaryKeySelective(userInfo);
		userIdentMapper.updateByPrimaryKeySelective(userIdent);
	}


	@Override
	public boolean insertUserBank(Long uid,UserBank userBank) {
		// TODO Auto-generated method stub
		UserBank bank = userBankMapper.selectUserCurrentBankCard(uid);
		if (bank!=null&&bank.getCardNo().equals(userBank.getCardNo())) {
			log.error("绑卡已绑定，用户={}",uid);
			return false;
		}
		//1.更新绑卡认证状态
		UserIdent _ident=new UserIdent();
		_ident.setUid(uid);
		_ident.setBindbank(2);
		_ident.setBindbankTime(new Date());
		userIdentMapper.updateByPrimaryKeySelective(_ident);

		//2.把之前的老卡无效
		userBankMapper.updateUserOldCardInvaild(uid);
		
		userBankMapper.insertSelective(userBank);
		return true;
	}

    @Override
    public String saveRealNameAuthInfo(JSONObject jsonObject, Long uid) {

        String livingFilePath = OSSUtil.uploadAuthImage(jsonObject.getString("living_photo"), ".jpg");
	    if(StringUtils.isBlank(livingFilePath)){
            return "上传活体认证失败";
        }

        String idCardBackFilePath = OSSUtil.uploadAuthImage(jsonObject.getString("idcard_back_photo"), ".jpg");
        if(StringUtils.isBlank(idCardBackFilePath)){
            return "上传身份证背面失败";
        }

        String idCardFrontPhotoFile = OSSUtil.uploadAuthImage(jsonObject.getString("idcard_front_photo"), ".jpg");
        if(StringUtils.isBlank(idCardFrontPhotoFile)){
            return "上传身份证正面失败";
        }

        UserIdent userIdentUpd = new UserIdent();
        userIdentUpd.setUid(uid);
        userIdentUpd.setLiveness(2);
        userIdentUpd.setRealName(2);//有盾实名和活体一起认证的
        userIdentUpd.setLivenessTime(new Date());
        userIdentUpd.setRealNameTime(new Date());

        User user = new User();
        user.setId(uid);
        user.setUserName(jsonObject.getString("id_name"));
        user.setUserCertNo(jsonObject.getString("id_number"));
        user.setIa(jsonObject.getString("issuing_authority"));
        user.setIndate(jsonObject.getString("validity_period"));
        user.setAddress(jsonObject.getString("address"));
        user.setNation(jsonObject.getString("nation"));
        user.setImgFace(livingFilePath);
        user.setImgCertBack(idCardBackFilePath);
        user.setImgCertFront(idCardFrontPhotoFile);

        updateUserRealName(user, userIdentUpd);

	    return null;
    }

	@Override
	public UserInfo selectUserInfo(Long uid) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUid(uid);
		return userInfoMapper.selectByPrimaryKey(userInfo);
	}

    @Override
    public List<User> selectUserByCertNo(String certNo) {
        return userMapper.selectAllByCertNo(certNo);
    }

	@Override
	public List<User> selectUserByPhone(String phone) {
		return userMapper.selectAllByPhone(phone);
	}

	@Override
	public void pvTotal(Long userId, String merchant, String loanMarketUrl) {
		if (null == userId || StringUtils.isEmpty(merchant)) {
			log.info("参数为空");
			throw new RuntimeException("参数为空");
		}
		LoanMarketStat loanMarketStat = new LoanMarketStat();
		loanMarketStat.setMerchant(merchant);
		loanMarketStat.setStatDate(TimeUtils.parseTime(new Date(), TimeUtils.dateformat2));
		//查询今天改商品的点击统计记录
		LoanMarketStat loanMarketStat4Query = loanMarketStatMapper.selectOne(loanMarketStat);
		log.info("#[查询今天改商品的点击统计记录]-loanMarketStat4Query={}", JSONObject.toJSON(loanMarketStat4Query));
		if (null == loanMarketStat4Query) {
			loanMarketStat.setLoanMarketUrl(loanMarketUrl);
			loanMarketStat.setUpdateTime(TimeUtils.parseTime(new Date(), TimeUtils.dateformat1));
			loanMarketStat.setLoanMarketPv(1);
			loanMarketStat.setLoanMarketUv(1);
			loanMarketStatMapper.insertSelective(loanMarketStat);
		} else {
			loanMarketStat4Query.setLoanMarketUrl(loanMarketUrl);
			loanMarketStat4Query.setUpdateTime(TimeUtils.parseTime(new Date(), TimeUtils.dateformat1));
			//同一个人只算一次
			String userId4Redis = redisMapper.get(RedisConst.PV_UV_Total + userId);
			if (StringUtils.isEmpty(userId4Redis)) {
				redisMapper.set(RedisConst.PV_UV_Total + userId, userId, 24 * 60 * 60);
				loanMarketStatMapper.updateLoanMarketStatById(loanMarketStat4Query);
			} else {
				loanMarketStatMapper.updatePvTotalById(loanMarketStat4Query);
			}
		}
	}
}
