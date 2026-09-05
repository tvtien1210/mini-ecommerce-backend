//Lấy form login thông qua id ="loginForm"
//Khi người dùng bấm nút Login (submit form), thì chạy funtion phía dưới

//Lay DOM loginForm
const loginFormElement = document.getElementById("loginForm");

const usernameInput = document.getElementById("username");

const passwordInput = document.getElementById("password");

//Tao su kien khi an submit
loginFormElement.addEventListener("submit", async function(e){
    //Ngăn trình duyệt reload lại trang mặc định của form
    //prevent: ngăn chặn, ngăn ngừa
    //Nếu không có dòng này, form sẽ submit theo HTML truyền thống, chứ không phải thymeleaf
    e.preventDefault();

    //Gửi request POST tới Spring Boot API login
    //Endpoint này xử lý authentication và tạo JWT token
    //await fetch dùng để gọi một yêu cầu mạng (như lấy dữ liệu từ một API) và
    // await : dừng lại chờ server phản hồi xong rồi mới lưu vào biến 'response'
    const response = await fetch("/api/auth/login",{
        //HTTP method sử dụng là POST
        //Vì @PostMapping("/login")
        method: "POST",

        //Báo cho server biết body gửi lên là JSON {username, password}
        //application: them vao, chen vao
        headers: {"Content-Type":"application/json"},

        //Convert object JavaScript thành JSON String
        body: JSON.stringify(
        {
        username:usernameInput.value,
        password:passwordInput.value
        })

    })

    const loginErrorElement = document.getElementById("loginError");
    const loginErrorMessageElement = document.getElementById("loginErrorMessage");

    //Kiểm tra HTTP status code
    if(!response.ok){
    loginErrorMessageElement.innerText=`Error: Username or password incorrect`;
    loginErrorElement.classList.remove("d-none");
    return;
    }


    //Login thành công
    //Chuyển ngừoi dùng về trang Home
    window.location.href="/";
});


// TỰ ĐỘNG ĐIỀN USER PASSWORD KHI LOGIN DEMO

function useDemoAccount(username, password) {

    // Điền username vào form
    document.getElementById("username").value = username;

    // Điền password vào form
    document.getElementById("password").value = password;

}
