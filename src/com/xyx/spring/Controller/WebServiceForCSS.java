package com.xyx.spring.Controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyx.spring.Model.User;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import net.sf.json.JSONObject;

/**
 * @moudle: WebServiceForCSS
 * @version:v1.0
 * @Description: TODO
 * @author: xuyuxiang
 * @date: 2018年2月28日 13:37:30
 *
 */
@Controller
public class WebServiceForCSS {

    @ResponseBody
    @RequestMapping(value = "getUserById", method = RequestMethod.GET, produces = {"application/json; charset=utf-8","application/xml"})
    @ApiOperation(value = "通过ID查询USER信息", httpMethod = "GET", notes = "暂无")
    public String getUserById(
            @ApiParam(required = true, name = "id", value = "ID") 
            @RequestParam(value = "id") Long id,HttpServletRequest request) {
        User user = new User();
        user.setId(id);
        user.setUserName("测试人员");
        user.setPassword(123456+"");
        JSONObject object = JSONObject.fromObject(user);
        return object.toString();
    }
}