import {
getUsername,
getRoles,
getExpired,
isLoggedIn,
isAdmin,
isCustomer,
logout} from "./auth.js";

document.addEventListener("DOMContentLoaded", function (){

//userInfo DOM
const userInfo = document.getElementById("userInfo");

//adminMenu DOM
const adminMenu = document.getElementById("adminMenu");

//goi username để chèn vào navbar loggedIn
const username = getUsername();

//gọi loggedIn để viết hàm điều kiện
const loggedIn = isLoggedIn();

    //nếu chưa login -> điều hướng về /login
    if(!loggedIn){
    userInfo.innerHTML=`
        <a class = "nav-link text-white" href="/login">
        <i class = "bi bi-person-circle"></i>
        Login
        </a>

    `;
    return;
    }

    //nếu loggedIn rồi
    //chèn username vào navbar userInfo
    if(isCustomer()){

    userInfo.innerHTML = `

        <div class="dropdown">

            <a class="nav-link text-white dropdown-toggle"
               href="#"
               data-bs-toggle="dropdown">

                <i class="bi bi-person-circle me-1"></i>

                ${username}

            </a>


            <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                <li>
                    <a class="dropdown-item text-start" href="#">
                    <i class="bi bi-person me-2"></i>
                        Profile

                    </a>
                </li>


                <li>
                    <a class="dropdown-item text-start" href="/myorders">
                    <i class="bi bi-bag-check me-2"></i>
                        My Orders

                    </a>
                </li>


                <li>
                    <hr class="dropdown-divider">
                </li>


                <li>
                    <a class="dropdown-item text-center"
                       href="#"
                       id="logoutBtn">
                       Logout
                       <i class="bi bi-box-arrow-right ms-2"></i>
                    </a>
                </li>

            </ul>

        </div>

    `;
    }



    //nếu isAdmin() la true,  thì ẩn d-none để admin hiện lên

    if(isAdmin()){

    const adminMenu = document.getElementById("adminMenu");
    adminMenu.classList.remove("d-none");
    userInfo.classList.add("d-none");

    adminMenu.innerHTML = `

            <div class="dropdown">

                <a
                    class="nav-link text-white dropdown-toggle"
                    href="#"
                    role="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false">

                    <i class="bi bi-speedometer2"></i>
                    Admin

                </a>


                <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                    <li>
                        <a
                            class="dropdown-item"
                            href="/admin">

                            <i class="bi bi-speedometer2 me-2"></i>
                            Admin Dashboard

                        </a>
                    </li>


                    <li>
                        <a
                            class="dropdown-item"
                            href="/admin/products">

                            <i class="bi bi-box-seam me-2"></i>
                            Manage Products

                        </a>
                    </li>


                    <li>
                        <a
                            class="dropdown-item"
                            href="/admin/orders">

                            <i class="bi bi-cart-check me-2"></i>
                            Manage Orders

                        </a>
                    </li>


                    <li>
                        <a
                            class="dropdown-item"
                            href="/admin/users">

                            <i class="bi bi-people me-2"></i>
                            Manage Users

                        </a>
                    </li>

                    <li>
                            <hr class = "dropdown-divider">
                    </li>
                    <li>
                            <a class="dropdown-item text-center"
                            href="#"
                            id="logoutBtn">
                            Logout
                            <i class="bi bi-box-arrow-right ms-2"></i>
                            </a>
                     </li>

                </ul>

            </div>
        `
    }

 //id=logoutBtn DOM, logout sau khi đăng nhập và hiển thị navbar theo navbar.js trong home.html
    const logoutBtn = document.getElementById("logoutBtn");
    if(logoutBtn){
    logoutBtn.addEventListener("click", function(e){
        e.preventDefault(); //ngăn không cho tự động trở về trình duyệt
        logout();
    });
    }


})