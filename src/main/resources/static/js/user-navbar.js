//Lấy username

//Chờ toàn bọ HTML load xong
//Sau khi DOM sẵn sàng thì  mới chạy code bên trong
document
    .addEventListener("DOMContentLoaded",()=>{

    //Lấy JWT accessToken đã lưu trong local
    //Nếu user chưa login thì sẽ là null
    const accessToken = localStorage.getItem("accessToken");
    //console.log("JWT accessToken =", accessToken)

    //Lấy vùng hiển thị thông tin user trên Navbar
    //home.html -> id="userInfo"
    const userInfo = document.getElementById("userInfo");
    //bảo vệ lỗi Cannot set properties of null, lỗi không tìm thấy id userInfo
    if(!userInfo){return}

    //Nếu chưa Login
    if(!accessToken){
    return;
    }

    //fetch().then(response => {}
    //fetch() gửi HTTP request tới Spring Boot API
    //"api/users/me" sẽ biết user nào đang login
    //Để ý dấu / ở đầu api
    fetch("/api/users/me",{

    //@GetMapping("/me")
    method: "GET",

    //Gửi accessToken tại local lên server
    //JWT trong Spring Security sẽ đọc header: phần json này
    //rồi xác thực user
    //Nhớ có một khoảng trắng sau Bearer
    headers:{"Authorization":"Bearer "+accessToken}
    })

    //Nhận response từ Spring Boot
    .then(response => {

        //Check http status code
        if(!response.ok){
        throw new Error("Get user failed");
        }


        //Chuyển response JSON từ server thành object Javascript
        //trả vể json
        return response.json();

    })

    //Nhận user từ Spring Boot
    //Để ý dấu chấm trước then
    .then(user=>{
    //VD user.username = chantaro
    //Thay đổi nội dung bên trong id = userInfo trong html
    //từ icon login -> 🫂 chantaro 🔽
    //có menu Profile/Logout

    //` ` → "shift @" -> template literal (cho phép ${}) , tránh nhầm "" hay ''

    userInfo.innerHTML=`
      <!-- Bootstrap dropdown -->
            <div class="dropdown">
                <!--Nút account trên Navbar data-bs-toggle= "dropdown" là Bootstrap JS xử lý mở menu-->
                <a
                class="nav-link text-white dropdown-toggle"
                href="#"
                data-bs-toggle="dropdown">
                    <!-- Icon account -->
                    <i class="bi bi-person-circle fs-5"></i>
                    <!--
                        Hiển thị username lấy từ database
                        Ví dụ:
                        chantaro
                    -->
                    ${user.username}
                </a>




                <!--
                    Menu xổ xuống khi click account

                -->

                <ul class="dropdown-menu dropdown-menu-end">



                    <!-- Profile -->

                    <li>

                        <a
                        class="dropdown-item"
                        href="/profile">

                        Profile

                        </a>

                    </li>




                    <!-- Logout -->

                    <li>


                        <button
                        class="dropdown-item"
                        id = "logoutBtn">

                        Logout


                        </button>


                    </li>


                </ul>


            </div>

    `;

    document
            .getElementById("logoutBtn")
            .addEventListener("click",()=>{
            //Xoa JWT accessToken
            localStorage.removeItem("accessToken");
            //Quay ve login
            window.location.href="/login"
            })
    })


    });

//kiểm tra login + hiển thị username trên Navbar
