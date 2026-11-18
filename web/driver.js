const token = sessionStorage.getItem('bm_token') || '';
const currentUser = JSON.parse(sessionStorage.getItem('bm_user') || 'null');
if (!token || !currentUser || currentUser.role !== 'driver') {
  window.location.href = '/';
}
let toastTimer;
const $ = s => document.querySelector(s);
const navItems = [
  ['dashboard','▦','Dashboard'],
  ['bus','▣','Assigned Bus'],
  ['route','⌖','Assigned Route'],
  ['update','↻','Update Trip'],
  ['breakdown','⚙','Breakdown Report'],
  ['accident','!','Accident Report'],
  ['replacement','⇄','Replacement Bus']
];
function notify(message){const t=$('#toast');t.textContent=message;t.classList.add('show');clearTimeout(toastTimer);toastTimer=setTimeout(()=>t.classList.remove('show'),3200)}
function esc(v){return String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}
async function api(path,method='GET',data=null){
  const opt={method,headers:{'X-Session-Token':token}};
  if(data){opt.headers['Content-Type']='application/x-www-form-urlencoded;charset=UTF-8';opt.body=new URLSearchParams(data).toString()}
  const r=await fetch(path,opt);let j={};try{j=await r.json()}catch{}
  if(r.status===401){logout();return;}
  if(!r.ok||j.success===false)throw new Error(j.message||'Something went wrong.');return j;
}
async function logout(){try{await api('/api/logout','POST',{})}catch{}sessionStorage.removeItem('bm_token');sessionStorage.removeItem('bm_user');window.location.href='/';}
function heading(text){$('#heading').textContent=text;document.querySelectorAll('#nav button').forEach(b=>b.classList.toggle('active',b.dataset.view===textKey(text)))}
function textKey(label){const item=navItems.find(x=>x[2]===label);return item?item[0]:label.toLowerCase()}
function statsCard(label,value,icon){return `<div class="stat"><div class="stat-top"><span>${esc(label)}</span><span class="icon">${icon}</span></div><strong>${esc(value)}</strong><small>Current records in shared text files</small></div>`}
function panel(title,inner,action=''){return `<div class="panel"><div class="panel-head"><div><h3>${esc(title)}</h3></div>${action}</div>${inner}</div>`}
function table(headers,rows){if(!rows.length)return `<div class="empty">No records to display yet.</div>`;return `<div class="table-wrap"><table><thead><tr>${headers.map(h=>`<th>${esc(h)}</th>`).join('')}</tr></thead><tbody>${rows.map(row=>`<tr>${row.map(v=>`<td>${v}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`}
async function openView(id){const item=navItems.find(x=>x[0]===id);heading(item?item[2]:'Dashboard');try{if(id==='dashboard')return driverHome();return driverView(id)}catch(e){notify(e.message)}}
async function driverHome(){
  const d=await api('/api/driver/dashboard');
  const bus=d.bus||{};const trip=d.trip||{};const trips=d.availableTrips||[];
  $('#view').innerHTML=`
    <div class="welcome"><h3>Good day, ${esc(currentUser.name)} (Licence: ${esc(d.licenseNumber||'—')}).</h3><p>Here is your assigned service and today's trip information.</p></div>
    <div class="stats">${statsCard('Driver ID',d.driverId,'♙')}${statsCard('Assigned Bus',bus.registrationNumber||'Not assigned','▣')}${statsCard('Trip Status',trip.status||'Not assigned','↻')}${statsCard('Trip ID',trip.tripId||'—','⌖')}</div>
    ${panel('Assigned trip',`<div class="grid-form"><div><label>Route</label><b>${esc((d.route||{}).startLocation||'—')} → ${esc((d.route||{}).endLocation||'—')}</b></div><div><label>Departure time</label><b>${esc(trip.departureTime||'—')}</b></div><div><label>Current location</label><b>${esc(trip.currentLocation||'—')}</b></div><div><label>Bus type</label><b>${esc(bus.busType||'—')}</b></div></div>`)}
    ${trips.length ? panel('Select / Switch Active Trip', `<form id="switchTripForm" class="form-card"><div class="grid-form"><div class="field"><label>Available Scheduled Trips</label><select name="tripId">${trips.map(t=>`<option value="${esc(t.tripId)}" ${String(t.tripId)===String(trip.tripId)?'selected':''}>${esc(t.label)}</option>`).join('')}</select></div></div><div class="form-actions"><button class="btn primary">Set Active Trip</button></div></form>`) : ''}
    <div class="notice">Your updates and reports are saved to shared-data. The Bus Owner can view them immediately.</div>
  `;
  const sf = $('#switchTripForm');
  if (sf) {
    sf.onsubmit = async e => {
      e.preventDefault();
      try {
        const res = await api('/api/driver/assign-trip', 'POST', Object.fromEntries(new FormData(sf)));
        notify(res.message);
        driverHome();
      } catch (err) {
        notify(err.message);
      }
    };
  }
}
async function driverView(id){const d=await api('/api/driver/dashboard');const bus=d.bus||{},route=d.route||{},trip=d.trip||{};
if(id==='bus'){$('#view').innerHTML=panel('Assigned bus',table(['Bus ID','Registration','Type','Capacity','Status','Speed'],[[esc(bus.busId||'—'),esc(bus.registrationNumber||'—'),esc(bus.busType||'—'),esc(bus.capacity||'Not provided'),esc(bus.status||'—'),esc(bus.speed||'Not provided')]]));return}
if(id==='route'){$('#view').innerHTML=panel('Assigned route',table(['Trip ID','Route ID','From','To','Departure','Current location','Status'],[[esc(trip.tripId||'—'),esc(route.routeId||'—'),esc(route.startLocation||'—'),esc(route.endLocation||'—'),esc(trip.departureTime||'—'),esc(trip.currentLocation||'—'),`<span class="pill">${esc(trip.status||'—')}</span>`]]));return}
if(id==='update'){$('#view').innerHTML=panel('Update trip status and location',`<div class="notice">Assigned bus: <b>${esc(bus.registrationNumber||'Not assigned')}</b> · Trip: <b>${esc(trip.tripId||'—')}</b></div><form id="actionForm" data-action="trip" class="form-card"><div class="grid-form"><div class="field"><label>Driver ID</label><input value="${esc(d.driverId)}" readonly></div><div class="field"><label>Current location</label><input name="location" required placeholder="Enter your current location"></div><div class="field"><label>Trip status</label><select name="status"><option>Ongoing</option><option>Completed</option><option>Cancelled</option></select></div></div><div class="form-actions"><button class="btn primary">Save trip update</button></div></form>`);return}
if(id==='breakdown'){$('#view').innerHTML=panel('Submit breakdown report',`<form id="actionForm" data-action="breakdown" class="form-card"><div class="grid-form"><div><label>Driver ID</label><input value="${esc(d.driverId)}" readonly></div><div><label>Assigned bus</label><input value="${esc(bus.registrationNumber||'—')}" readonly></div><div><label>Breakdown type</label><select name="category"><option>Engine Starting Issue</option><option>Brake Squealing or Grinding</option><option>Engine Overheating</option><option>Vibration or Shaking While Driving</option><option>Battery Drain / Electrical Issues</option><option>Other</option></select></div><div><label>Severity</label><select name="severity"><option>Low</option><option>Medium</option><option>High</option></select></div></div><label>Description / additional details</label><textarea name="description" required placeholder="Describe the issue"></textarea><div class="form-actions"><button class="btn primary">Submit breakdown report</button></div></form>`);return}
if(id==='accident'){$('#view').innerHTML=panel('Submit accident report',`<form id="actionForm" data-action="accident" class="form-card"><div class="grid-form"><div><label>Driver ID</label><input value="${esc(d.driverId)}" readonly></div><div><label>Assigned bus</label><input value="${esc(bus.registrationNumber||'—')}" readonly></div><div><label>Accident location</label><input name="location" required></div><div><label>Severity</label><select name="severity"><option>Low</option><option>Medium</option><option>High</option></select></div></div><label>Accident details</label><textarea name="description" required></textarea><div class="form-actions"><button class="btn primary">Submit accident report</button></div></form>`);return}
if(id==='replacement'){$('#view').innerHTML=panel('Request a replacement bus',`<form id="actionForm" data-action="replacement" class="form-card"><div class="grid-form"><div><label>Driver ID</label><input value="${esc(d.driverId)}" readonly></div><div><label>Currently assigned bus</label><input value="${esc(bus.registrationNumber||'—')}" readonly></div></div><label>Reason for replacement</label><textarea name="reason" required placeholder="Explain why a replacement bus is needed"></textarea><div class="form-actions"><button class="btn primary">Send request</button></div></form>`);return}}
document.addEventListener('submit',async e=>{const form=e.target;if(form.id!=='actionForm')return;e.preventDefault();try{const data=Object.fromEntries(new FormData(form));const result=await api('/api/driver/'+form.dataset.action,'POST',data);notify(result.message||'Saved successfully.');openView(form.dataset.action==='trip'?'update':form.dataset.action)}catch(err){notify(err.message)}});
$('#profileName').textContent=currentUser.name;$('#signedName').textContent=currentUser.name;$('#avatar').textContent=currentUser.name.slice(0,1).toUpperCase();
$('#nav').innerHTML=navItems.map(([id,icon,label])=>`<button data-view="${id}"><span>${icon}</span>${label}</button>`).join('');
$('#nav').onclick=e=>{const b=e.target.closest('button[data-view]');if(b)openView(b.dataset.view)};
openView('dashboard');
