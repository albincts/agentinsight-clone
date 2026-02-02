import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { AdminSidebar } from "../admin-sidebar/admin-sidebar";
import { AgentService } from '../../../services/agent.service';

@Component({
  selector: 'app-performance',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AdminSidebar],
  templateUrl: './performance.html',
  styleUrls: ['./performance.css'],
})
export class Performance implements OnInit {
  filterForm: FormGroup;
  selectedAgentData: any = null;
  error = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private agentService: AgentService
  ) {
    this.filterForm = this.fb.group({
      agentId: ['']
    });
  }

  ngOnInit(): void {}

  // performance.ts
async applyFilters(): Promise<void> {
  this.error.set('');
  const agentId = this.filterForm.value.agentId?.trim();

  if (!agentId) {
    this.selectedAgentData = null;
    return;
  }

  try {
    // This will now hold the exact JSON structure you saw in Postman
    this.selectedAgentData = await this.agentService.getAgentPerformanceById(agentId);
    console.log('Received Data:', this.selectedAgentData);
  } catch (err) {
    this.selectedAgentData = null;
    this.error.set('Could not find performance data for this ID');
  }
}
}