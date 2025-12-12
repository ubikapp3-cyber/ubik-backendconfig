import { Routes } from '@angular/router';
import path from 'path';
import { Home } from './views/home/home';
import { LoginComponent } from './views/login/login.component';
import { RegisterComponent } from './views/register/register.component';

export const routes: Routes = [
    {path: "", component: RegisterComponent},
    {path: "register", component: RegisterComponent}
];
