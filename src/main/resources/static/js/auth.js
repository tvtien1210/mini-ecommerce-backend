//Tạo function getAccessToken
function getAccessToken(){
    //lấy accessToken từ localStorage
    return localStorage.getItem("accessToken");
}

//Tạo function getJwtPayload -> lấy riêng phần Payload trong Header.Payload.Signature

function getJwtPayload(){

    //tạo biến token, lấy getAccessToken từ function trên

    const token = getAccessToken();

    //nếu token sai (trong case token bị sai định dạng hay bị xoá null), dừng code

    if(!token){
    return null;
    }

    //nếu đúng try catch ném error nếu JWT error,
    try{

    //Tách payload bởi dấu ".", và [1] là lấy riêng phần payload đó
    //Giữa split() và [1] không có dấu .
    const base64Url = token.split(".")[1];

    // g: la global (tìm tất cả), nếu không có g, sẽ chỉ replace ký tự đầu tiên tìm thấy
    const base64 = base64Url.replace(/-/g,"+").replace(/_/g,"/");

    //ascii to binary (chuyển chuỗi mã hóa ASCII quay về dữ liệu gốc)
    const jsonPayload = atob(base64);

    //Lấy dữ liệu thuần qua JSON.parse ví dụ username, role
    return JSON.parse(jsonPayload);

    //Nhớ mở đóng ngoặc () ở catch (error)‼️
    }catch (error){
    //tồn tại nhưng không đúng định dạng
    console.error("Invalid JWT : ",error);
    return null;
    }

}

//Get Username
export function getUsername(){
    //Tạo biến payload
    const payload = getJwtPayload();

    //Nếu payload có dữ liệu trả vể thuộc tính sub
    //Nếu ko có dữ liệu, trả về null -> tránh bug, code dừng đột ngột
    return payload?.sub || null;
}

//Get Roles
export function getRoles(){
    const payload = getJwtPayload();
    return payload?.roles || null;
}

//Get Expired
export function getExpired(){
    const payload = getJwtPayload();
    return payload?.exp || null;
}

//isLoggedIn() : check xem đã đăng nhập chưa, có accessToken chưa?
export function isLoggedIn(){

    //Nếu không tồn tại getAccessToken -> return false
    if(!getAccessToken()){return false};

    //Nếu tồn tại lấy exp từ getExpired()
    const exp = getExpired();

    //Nếu không tồn tại exp -> return false
    if(!exp){return false};

    //Nếu tồn tại đổi sang exp sang mili giây để so sánh lớn hơn > với Date.now() cũng có đơn vị là mili giây
    //Nếu exp lớn hơn thời gian hiện tại, exp còn hạn -> isLoggedIn() == true
    console.log("Token expire at : " + (new Date(exp*1000)).toLocaleString("vi-VN"));
    return exp*1000 >Date.now();

}

//isAdmin: để bổ sung navbar Admin Dashboard
export function isAdmin(){
    const roles = getRoles();
    return roles?.includes("ROLE_ADMIN") || false;
}

//isUser: để thiết kế navbar ví dụ xem cart, lịch sử mua
export function isCustomer(){
    const roles = getRoles();
    return roles?.includes("ROLE_CUSTOMER") || false;
}

//logout
export function logout(){
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href="/login";
}

//console.log(getAccessToken());
//console.log(getJwtPayload());
//console.log(getRoles());
//console.log(getUsername());
//console.log(isLoggedIn());

//console.log(getExpired());
//const exp = getExpired();
//const date = new Date(exp*1000);
//console.log(date.toLocaleString("vi-VN"));

//console.log("isCustomer "+isCustomer());
//console.log("isAdmin "+isAdmin());






