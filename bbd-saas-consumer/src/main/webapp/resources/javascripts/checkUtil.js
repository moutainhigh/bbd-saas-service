/**
 * 验证手机号
 * @param sMobile
 * @returns {boolean}
 */
function checkMobile(sMobile) {
    var re = /^1\d{10}$/;
    if (re.test(sMobile)) {
        return true;
    } else {
        return false;
    }
}
/**
 * 验证邮箱
 * @param email
 * @returns {boolean}
 */
function checkemail(email) {            // 功能函数
    var re = /^(\w-*\.*)+@(\w-?)+(\.\w{2,})+$/
    if(re.test(email)){
        return true;
    }else{
        return false;
    }
}

/**
 * 验证电话号码 验证规则：区号+号码，区号以0开头，3位或4位 号码由7位或8位数字组成 区号与号码之间可以无连接符，也可以“-”连接 如01088888888,010-88888888,0955-7777777
 * @param phone
 * @returns {boolean}
 */
function checkPhone(phone){
    var re = /^0\d{2,3}-?\d{7,8}$/;
    if(re.test(phone)){
        return true;
    }else{
        return false;
    }
}