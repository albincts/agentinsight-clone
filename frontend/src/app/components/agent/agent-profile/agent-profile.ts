import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Sidebar } from '../../sidebar/sidebar';
import { AuthService } from '../../../services/auth.service';
import { AgentService } from '../../../services/agent.service';
import { Agent } from '../../../models/agent.model';
 
@Component({
  selector: 'app-agent-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, Sidebar],
  templateUrl: './agent-profile.html',
  styleUrl: './agent-profile.css',
})
export class AgentProfile implements OnInit {
  // Fix: Added the missing property for the template
  isReadOnly = true;
   
  original: Agent | null = null;
  model: Agent = { agentid: '', name: '', email: '', phone: '', role: 'agent' };
  saving = false;
  message = '';
 
  roles = ['agent', 'admin'];
 
  constructor(private authService: AuthService, private agentService: AgentService) {}
 
  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.loadUser(user.agentid);
    }
  }
 
  async loadUser(agentid: string): Promise<void> {
    const current = this.authService.currentUser();
    if (current) {
      this.original = { ...current };
      this.model = { ...current };
    }
  }
 
  // async save1(): Promise<void> {
  //   this.saving = true;
  //   this.message = '';
  //   try {
  //     // Profile update not implemented with backend - just update local state
  //     this.authService.setCurrentUser(this.model);
  //     this.original = { ...this.model };
  //     alert('Profile saved (local only - backend update not implemented)');
 
  //     // Fix: Lock the form again after successful save
  //     this.isReadOnly = true;
  //   } catch (err) {
  //     console.error(err);
  //     alert('Failed to save');
 
  //   } finally {
  //     this.saving = false;
  //   }
  // }
 
 
async save(): Promise<void> {
  this.saving = true;
  try {
    // Create a copy to modify without affecting the UI immediately
    const dataToSend = {
      ...this.model,
      role: this.model.role.toUpperCase() // Convert 'agent' to 'AGENT'
    };
 
    const updated = await this.agentService.updateAgent(dataToSend);
    this.original = { ...updated };
    this.authService.setCurrentUser(updated);
    alert('Profile saved successfully!');
    this.isReadOnly = true;
  } catch (err) {
    console.error("Full Error Object:", err);
    alert('Failed to save: Check console for details');
  } finally {
    this.saving = false;
  }
}
 
 
  cancel(): void {
    if (this.original) {
      this.model = { ...this.original };
      alert('Failed to save');
     
    }
    // Fix: Lock the form when canceling
    this.isReadOnly = true;
  }
 
  // Optional: Helper method for the template button
  toggleEdit(): void {
    this.isReadOnly = !this.isReadOnly;
    if (!this.isReadOnly) {
      this.message = ''; // Clear status messages when starting to edit
    }
  }
}