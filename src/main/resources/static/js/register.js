
//const myForm = document.getElementById("registerForm");
//console.log(myForm);


// 1.GET DOM ELEMENT

//REGISTER FORM ELEMENTS
const registerForm = document.getElementById("registerForm");


//INPUT ELEMENT
const fullNameInput = document.getElementById("fullName");
const usernameInput = document.getElementById("username");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");

//UI REGISTER ELEMENTS
const registerFormContainer = document.getElementById("registerFormContainer");
const registerError = document.getElementById("registerError");
const registerErrorMessage = document.getElementById("registerErrorMessage");
const registerSuccess = document.getElementById("registerSuccess");

//CONTINUE LOGIN BUTTON
const continueLoginButton = document.getElementById("continueLoginButton");

// 2.REGISTER FORM SUBMIT

    //SUBMIT EVENT

    // async function là hàm giúp tải dữ liệu ngầm mà không làm đơ ứng dụng
    //await duoc dung ben trong async
    registerForm.addEventListener("submit", async function(e){ // <-- MỞ BLOCK

    //Ngan chan browser reload theo kieu HTML goc
     e.preventDefault();

    //Get values from input
    //trim() cắt bỏ các dấu cách (khoảng trắng) dư thừa ở đầu và cuối chuỗi.
    const fullName = fullNameInput.value.trim();
    const username = usernameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value; //pw khong trim(), vi space có thể coi là 1 ký tự trong pw
    const confirmPassword = confirmPasswordInput.value;

    //Check password Validation
    //Xác nhận tính hợp lệ của dữ liệu khách nhập vào form register
    if(password !== confirmPassword){
    registerErrorMessage.innerText=`Error: Password do not match`;
    registerError.classList.remove("d-none");
    return;
    }

    //Create request date
    //Tạo yêu cầu theo kiểu json để gửi về backend
    const requestData = {
     fullName:fullName,
     username:username,
     email:email,
     password:password,
     confirmPassword:confirmPassword
    }

    //Call Backend API
    //Gọi backend xử lý yêu cầu API khách gửi lên

    try{
        //send POST request
        const response = await fetch("/api/auth/register",{

        method:"POST",

        headers: {"Content-Type":"application/json"},

        body: JSON.stringify(
        requestData)
        });

        //convert response to JSON
        const data = await response.json();

        //check HTTP status VD 403 error, 200 ok..
        if(!response.ok){
        registerErrorMessage.innerText=`Error : ${data.message}`;
        registerError.classList.remove("d-none");
        return;
        }


        document.getElementById("registerSuccessUserDetail").innerText =

        // Template Strings `backtick`
        `
        Username : ${data.username}
        Email : ${data.email}
        `
        ;

        //Register Success
        registerFormContainer.classList.add("d-none");
        //Show Success UI
        registerSuccess.classList.remove("d-none");

    }catch (error){
        //Log error
        console.error("Lỗi fetch API"+error);

        //Show error UI
        registerError.classList.remove("d-none");

        //Display network error message
        registerErrorMessage.innerText = "Lỗi gửi và lấy dữ liệu từ máy chủ"

    }
    } // --> ĐÓNG BLOCK
);

    // 2.CONTINUE TO LOGIN
    //Xử lý khi click nút Login
    //"button" không phải event mà trình duyệt phát ra, nên sẽ dùng click, và không cần await
    continueLoginButton.addEventListener("click", function(e){
    window.location.href="/login";
    });







