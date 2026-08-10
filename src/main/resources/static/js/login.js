//Lấy form login thông qua id ="loginForm"
//Khi người dùng bấm nút Login (submit form), thì chạy funtion phía dưới

//const myForm = document.getElementById("loginForm");
//console.log(myForm);

document
.getElementById("loginForm")
.addEventListener("submit", async function(e){
    //Ngăn trình duyệt reload lại trang mặc định của form
    //prevent: ngăn chặn, ngăn ngừa
    //Nếu không có dòng này, form sẽ submit theo HTML truyền thống, chứ không phải thymeleaf
    e.preventDefault();

    //Lấy username người dùng nhập trong input id = "username"
    const username = document.getElementById("username").value;

    //Lấy password người dùng nhập trong input id = "password"
    const password = document.getElementById("password").value;

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
        username:username,
        password:password
        })

    })


    const loginError = document.getElementById("loginError");
    const loginErrorMessage = document.getElementById("loginErrorMessage");
    //Kiểm tra HTTP status code
    if(!response.ok){
    loginErrorMessage.innerText=`Error: Username or password incorrect`;
    loginError.classList.remove("d-none");
    return;
    }

    //Đọc response từ Spring Boot
    //Server trả về JSON chứa JWT token
    const data = await response.json();

    //Lưu JWT token vào LocalStorage của trình duyệt
    localStorage.setItem("accessToken",data.accessToken);
    localStorage.setItem("refreshToken",data.refreshToken);


    //Login thành công
    //Chuyển ngừoi dùng về trang Home
    window.location.href="/";
});


