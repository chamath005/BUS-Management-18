const token = sessionStorage.getItem('bm_token') || '';
const currentUser = JSON.parse(sessionStorage.getItem('bm_user') || 'null');
if (!token || !currentUser || currentUser.role !== 'owner') {
  window.location.href = '/';
}
let toastTimer;
const $ = s => document.querySelector(s);
const navItems = [
  ['dashboard','▦','Dashboard'],
  ['buses','▣','Bus Management'],
  ['routes','⌖','Route Management'],
  ['trips','↻','Trip Management'],
  ['users','☺','User Management'],
  ['passengers','♙','Passengers'],
  ['tripUpdates','◷','Driver Trip Updates'],
  ['breakdowns','⚙','Breakdown Reports'],
  ['accidents','!','Accident Reports'],
  ['replacements','⇄','Replacement Requests'],
  ['locations','⌖','Driver Location History']
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
async function openView(id){const item=navItems.find(x=>x[0]===id);heading(item?item[2]:'Dashboard');try{if(id==='dashboard')return ownerHome();if(id==='users')return ownerUsersView();return ownerView(id)}catch(e){notify(e.message)}}
async function ownerHome(){const d=await api('/api/owner/summary');$('#view').innerHTML=`<div class="welcome"><h3>Welcome, ${esc(currentUser.name)}.</h3><p>Manage your buses, routes, trips, users, and driver communications.</p></div><div class="stats">${statsCard('Total buses',d.buses,'▣')}${statsCard('Total routes',d.routes,'⌖')}${statsCard('Total trips',d.trips,'↻')}${statsCard('Registered users',d.users,'☺')}</div>${panel('Driver communication',table(['Breakdown reports','Accident reports','Replacement requests','Pending requests','Trip updates'],[[d.breakdowns,d.accidents,d.requests,d.pendingRequests,d.tripUpdates]]))}<div class="notice">All modules share the unified shared-data folder. Updates appear here immediately.</div>`}

async function ownerUsersView(){
  const [usersRes, driversRes, tripsRes, assignRes] = await Promise.all([
    api('/api/owner/data?section=users'),
    api('/api/owner/data?section=drivers'),
    api('/api/owner/data?section=trips'),
    api('/api/owner/data?section=assignments')
  ]);
  const users = usersRes.rows || [];
  const drivers = driversRes.rows || [];
  const trips = tripsRes.rows || [];
  const assigns = assignRes.rows || [];

  $('#view').innerHTML = `
    <div class="panel form-card">
      <div class="panel-head"><h3>Add New User Account</h3></div>
      <form id="addUserForm">
        <div class="grid-form">
          <div class="field"><label>Full Name</label><input name="name" required placeholder="e.g. Kasun Perera"></div>
          <div class="field"><label>Email Address</label><input name="email" type="email" required placeholder="kasun@example.com"></div>
          <div class="field"><label>Password</label><input name="password" type="password" minlength="4" required placeholder="Min 4 characters"></div>
          <div class="field"><label>Role</label><select name="role"><option value="driver">Driver</option><option value="conductor">Conductor</option><option value="passenger">Passenger</option><option value="owner">Bus Owner</option></select></div>
          <div class="field"><label>Phone Number</label><input name="phone" placeholder="07XXXXXXXX"></div>
          <div class="field"><label>Licence / Employee ID (Optional)</label><input name="extra" placeholder="e.g. B987654 or CON002"></div>
        </div>
        <div class="form-actions"><button class="btn primary">Add User</button></div>
      </form>
    </div>

    ${panel('Registered System Users (shared-data/users.txt)', table(
      ['User ID','Name','Email','Role','Phone','Licence / Emp ID','Actions'],
      users.map(u => [
        esc(u.userId), esc(u.name), esc(u.email),
        `<span class="pill">${esc(u.role)}</span>`, esc(u.phone || '—'), esc(u.extra || '—'),
        `<div class="actions"><button class="action delete" data-del-user="${esc(u.userId)}">Delete</button></div>`
      ])
    ))}

    <div class="panel form-card">
      <div class="panel-head"><h3>Assign Trip to Driver</h3></div>
      <form id="assignDriverForm">
        <div class="grid-form">
          <div class="field">
            <label>Select Driver</label>
            <select name="driverId" required>
              ${drivers.map(d => `<option value="${esc(d.driverId)}">#${esc(d.driverId)} · ${esc(d.name)} (${esc(d.license)})</option>`).join('')}
            </select>
          </div>
          <div class="field">
            <label>Select Trip</label>
            <select name="tripId" required>
              ${trips.map(t => `<option value="${esc(t.tripId)}">Trip #${esc(t.tripId)} · Bus #${esc(t.busId)} · ${esc(t.departureTime)}</option>`).join('')}
            </select>
          </div>
        </div>
        <div class="form-actions"><button class="btn primary">Assign Driver</button></div>
      </form>
    </div>

    ${panel('Active Driver Trip Assignments', table(
      ['Driver ID','Driver Name','Assigned Trip ID','Bus Registration'],
      assigns.map(a => [esc(a.driverId), esc(a.driverName), esc(a.tripId), esc(a.busNumber || '—')])
    ))}
  `;

  $('#addUserForm').onsubmit = async e => {
    e.preventDefault();
    try {
      const res = await api('/api/owner/add-user', 'POST', Object.fromEntries(new FormData(e.target)));
      notify(res.message || 'User added successfully.');
      ownerUsersView();
    } catch (err) {
      notify(err.message);
    }
  };

  $('#assignDriverForm').onsubmit = async e => {
    e.preventDefault();
    try {
      const res = await api('/api/owner/assign-driver', 'POST', Object.fromEntries(new FormData(e.target)));
      notify(res.message || 'Driver assigned.');
      ownerUsersView();
    } catch (err) {
      notify(err.message);
    }
  };

  $('#view').querySelectorAll('[data-del-user]').forEach(b => b.addEventListener('click', async () => {
    if (!confirm('Delete this user account?')) return;
    try {
      await api('/api/owner/delete', 'POST', { type: 'users', id: b.dataset.delUser });
      notify('User deleted.');
      ownerUsersView();
    } catch (err) {
      notify(err.message);
    }
  }));
}

function ownerForm(type,row={}){const fields=type==='buses'?[['busId','Bus ID','number'],['registrationNumber','Registration number','text'],['capacity','Capacity','number'],['status','Status','text'],['speed','Speed','number'],['busType','Bus type','text']]:type==='routes'?[['routeId','Route ID','number'],['startLocation','Starting location','text'],['endLocation','Ending location','text'],['distance','Distance (km)','number']]:[['tripId','Trip ID','number'],['busId','Bus ID','number'],['routeId','Route ID','number'],['departureTime','Departure time','text'],['status','Status','text']];
return `<div class="panel form-card"><div class="panel-head"><h3>${row._edit?'Update':'Add'} ${type.slice(0,-1)}</h3></div><form id="entityForm" data-type="${type}"><div class="grid-form">${fields.map(([k,l,t])=>`<div class="field"><label>${l}</label><input name="${k}" type="${t}" value="${esc(row[k]??'')}" ${k.endsWith('Id')?'required':''} ${k==='busId'&&type==='trips'?'required':''}></div>`).join('')}</div><div class="form-actions"><button class="btn primary">Save</button><button type="button" class="btn secondary" onclick="openView('${type}')">Clear</button></div></form></div>`}
const ownerMeta={buses:{title:'Bus Management',cols:['Bus ID','Registration','Capacity','Status','Speed','Type'],keys:['busId','registrationNumber','capacity','status','speed','busType']},routes:{title:'Route Management',cols:['Route ID','From','To','Distance (km)'],keys:['routeId','startLocation','endLocation','distance']},trips:{title:'Trip Management',cols:['Trip ID','Bus ID','Route ID','Departure','Status','Current location'],keys:['tripId','busId','routeId','departureTime','status','currentLocation']},passengers:{title:'Passengers',cols:['Ticket ID','Passenger ID','Passenger name','Trip ID','Bus','Fare','Ticket time'],keys:['ticketId','passengerId','passengerName','tripId','busNumber','fare','ticketTime']},tripUpdates:{title:'Driver Trip Updates',cols:['Driver ID','Driver','Trip ID','Bus','Status','Location','Updated at'],keys:['driverId','driverName','tripId','busNumber','status','location','updatedAt']},breakdowns:{title:'Breakdown Reports',cols:['Report ID','Driver ID','Driver','Bus','Issue','Description','Severity','Date'],keys:['reportId','driverId','driverName','busNumber','category','description','severity','date']},accidents:{title:'Accident Reports',cols:['Report ID','Driver ID','Driver','Bus','Location','Details','Severity','Date'],keys:['reportId','driverId','driverName','busNumber','location','description','severity','date']},replacements:{title:'Replacement Requests',cols:['Request ID','Driver ID','Driver','Bus','Reason','Date','Status'],keys:['requestId','driverId','driverName','busNumber','reason','date','status']},locations:{title:'Driver Location History',cols:['Driver ID','Driver','Trip ID','Bus','Location','Updated at'],keys:['driverId','driverName','tripId','busNumber','location','updatedAt']}};
async function ownerView(id){const meta=ownerMeta[id];if(!meta)return;const result=await api('/api/owner/data?section='+encodeURIComponent(id));let rows=result.rows||[];let content='';if(['buses','routes','trips'].includes(id)){content=ownerForm(id)+panel(meta.title,table(meta.cols.concat(['Actions']),rows.map(r=>{const cells=meta.keys.map(k=>esc(r[k]??''));return [...cells,`<div class="actions"><button class="action" data-edit="${id}" data-id="${esc(r[meta.keys[0]])}">Edit</button><button class="action delete" data-delete="${id}" data-id="${esc(r[meta.keys[0]])}">Delete</button></div>`]})));}else if(id==='replacements'){content=panel(meta.title,table(meta.cols.concat(['Actions']),rows.map(r=>{const cells=meta.keys.map(k=>esc(r[k]??''));return [...cells,`<div class="actions"><button class="action" data-approve="${esc(r.requestId)}">Approve</button></div>`]})));}else content=panel(meta.title,table(meta.cols,rows.map(r=>meta.keys.map(k=>esc(r[k]??'')))));$('#view').innerHTML=content;$('#view').querySelectorAll('[data-edit]').forEach(b=>b.addEventListener('click',()=>{const row=rows.find(x=>String(x[meta.keys[0]])===b.dataset.id);const form=$('#entityForm');for(const [key,value] of Object.entries(row||{})){if(form.elements[key])form.elements[key].value=value??'';}const title=form.parentElement.querySelector('h3');if(title)title.textContent='Update '+id.slice(0,-1);}));$('#view').querySelectorAll('[data-delete]').forEach(b=>b.addEventListener('click',async()=>{if(!confirm('Delete this record?'))return;try{await api('/api/owner/delete','POST',{type:id,id:b.dataset.id});notify('Record deleted.');ownerView(id)}catch(e){notify(e.message)}}));$('#view').querySelectorAll('[data-approve]').forEach(b=>b.addEventListener('click',async()=>{try{await api('/api/owner/replacement-status','POST',{requestId:b.dataset.approve,status:'APPROVED'});notify('Replacement request approved.');ownerView(id)}catch(e){notify(e.message)}}));wireEntityForm()}
function wireEntityForm(){const form=$('#entityForm');if(!form)return;form.onsubmit=async e=>{e.preventDefault();try{const data=Object.fromEntries(new FormData(form));data.type=form.dataset.type;await api('/api/owner/save','POST',data);notify('Record saved successfully.');openView(form.dataset.type)}catch(err){notify(err.message)}}}
$('#profileName').textContent=currentUser.name;$('#signedName').textContent=currentUser.name;$('#avatar').textContent=currentUser.name.slice(0,1).toUpperCase();
$('#nav').innerHTML=navItems.map(([id,icon,label])=>`<button data-view="${id}"><span>${icon}</span>${label}</button>`).join('');
$('#nav').onclick=e=>{const b=e.target.closest('button[data-view]');if(b)openView(b.dataset.view)};
openView('dashboard');
